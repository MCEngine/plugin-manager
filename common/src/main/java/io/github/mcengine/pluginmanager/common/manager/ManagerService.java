package io.github.mcengine.pluginmanager.common.manager;

import io.github.mcengine.pluginmanager.api.manager.ChecksumMismatchException;
import io.github.mcengine.pluginmanager.api.manager.DesiredChange;
import io.github.mcengine.pluginmanager.api.manager.DesiredState;
import io.github.mcengine.pluginmanager.api.manager.InstalledPlugin;
import io.github.mcengine.pluginmanager.api.manager.ManagerClient;
import io.github.mcengine.pluginmanager.api.manager.PluginAction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * One poll: report what is installed, ask what should change, and apply it.
 *
 * <p>Free of Bukkit, so the whole flow is testable against a local HTTP server
 * and a temporary directory. The Bukkit side supplies the paths, the scheduling
 * and the command surface; the sequencing lives here.</p>
 */
public final class ManagerService {

    private final ManagerClient client;
    private final UpdateStaging staging;
    private final Path pluginsDirectory;
    private final String serverId;

    public ManagerService(
        ManagerClient client,
        Path pluginsDirectory,
        String serverId
    ) {
        this.client = Objects.requireNonNull(client, "client");
        this.pluginsDirectory = Objects.requireNonNull(pluginsDirectory, "pluginsDirectory");
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.staging = new UpdateStaging(pluginsDirectory);
    }

    /** The staging area, for a caller that wants to apply deletions at shutdown. */
    public UpdateStaging staging() {
        return staging;
    }

    /**
     * Reports the full inventory of installed plugins.
     *
     * @param platform    {@code spigot}, {@code paper} or {@code folia}
     * @param mcVersion   the running Minecraft version
     * @param agentVersion this plugin's own version
     * @return how many plugins were reported
     * @throws IOException if reading the directory or the request fails
     */
    public int report(String platform, String mcVersion, String agentVersion) throws IOException {
        // Checksums are read here because the panel uses them to tell an
        // identical version apart from one someone replaced by hand.
        List<InstalledPlugin> installed = PluginInventory.read(pluginsDirectory, true);
        client.report(serverId, installed, platform, mcVersion, agentVersion);
        return installed.size();
    }

    /**
     * Registers this Minecraft server and returns the id and key to store.
     *
     * <p>Returned rather than written: saving them means rewriting
     * {@code config.yml}, and Bukkit's {@code saveConfig()} drops every comment
     * in the file — which in this plugin's config is most of what it is for.
     * The operator pastes two values instead.</p>
     *
     * @param name what the panel should call this server
     * @return the generated server key
     * @throws IOException if the request fails
     */
    public String register(String name) throws IOException {
        return client.register(name);
    }

    /** The central server this service talks to. */
    public io.github.mcengine.pluginmanager.api.manager.CentralServer server() {
        return client.server();
    }

    /** Asks what should change, without applying any of it. */
    public DesiredState check() throws IOException {
        return client.desired(serverId);
    }

    /**
     * Applies every change in a desired state.
     *
     * <p>One failure does not stop the rest: a jar that fails its checksum is
     * reported and skipped, and the other four plugins on the server still get
     * their updates. Every outcome is reported back so the panel shows the same
     * thing the server operator sees.</p>
     *
     * @param state what to do
     * @return one outcome per change, in the order they were attempted
     */
    public List<ManagerOutcome> apply(DesiredState state) {
        List<ManagerOutcome> outcomes = new ArrayList<>();
        for (DesiredChange change : state.changes()) {
            outcomes.add(applyOne(change));
        }
        return outcomes;
    }

    /**
     * Applies one change.
     *
     * <p>Nothing here loads or unloads a plugin, because Bukkit cannot do that
     * safely. An install or an update is staged into {@code plugins/update/} for
     * the server to apply on its next start, and a delete is recorded and
     * performed at shutdown.</p>
     *
     * @param change the change
     * @return what happened
     */
    public ManagerOutcome applyOne(DesiredChange change) {
        try {
            if (change.action() == PluginAction.DELETE) {
                staging.markForDeletion(change.pluginId());
                reportOutcome(change.pluginId(), "pending_delete", null);
                return ManagerOutcome.staged(change,
                    change.pluginId() + " will be removed when the server stops.");
            }

            Path written = staging.stage(client, change);
            reportOutcome(change.pluginId(), "pending_update", null);

            String verb = change.action() == PluginAction.INSTALL ? "installed" : "updated";
            return ManagerOutcome.staged(change,
                change.pluginId() + " " + change.toVersion() + " staged in "
                    + written.getParent().getFileName() + "/ and will be " + verb
                    + " when the server restarts.");

        } catch (ChecksumMismatchException tampered) {
            // Its own branch, and deliberately not retried: a mismatch means the
            // bytes are not what was published, and fetching them again is as
            // likely to produce the same wrong file.
            reportOutcome(change.pluginId(), "failed", tampered.getMessage());
            return ManagerOutcome.failed(change,
                "Refused " + change.pluginId() + ": the download did not match the checksum "
                    + client.server().describe() + " published.");

        } catch (IOException failure) {
            reportOutcome(change.pluginId(), "failed", failure.getMessage());
            return ManagerOutcome.failed(change,
                "Could not " + change.describe() + ": " + failure.getMessage());
        }
    }

    /**
     * Reports an outcome, swallowing a failure to do so.
     *
     * <p>The work already happened. Failing the apply because the report of it
     * did not go through would leave the server in a state the panel disagrees
     * with <em>and</em> the operator without their update.</p>
     */
    private void reportOutcome(String pluginId, String state, String error) {
        try {
            client.reportOutcome(serverId, pluginId, state, error);
        } catch (IOException ignored) {
            // The next poll reports the inventory again, which reconciles this.
        }
    }
}
