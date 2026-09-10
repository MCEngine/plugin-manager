package io.github.mcengine.universal;

import io.github.mcengine.universal.api.TemplateRequest;
import io.github.mcengine.universal.api.TemplateResponse;
import io.github.mcengine.universal.api.TemplateService;
import io.github.mcengine.universal.common.DefaultTemplateService;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The single entry point to this project.
 *
 * <p><strong>If you are integrating against this plugin, this is the only file
 * you need to read.</strong> Every platform module — SpigotMC, PaperMC, Folia,
 * the universal engine jar, and the Forge, Fabric, and NeoForge mods — goes
 * through this class, and so should your plugin.</p>
 *
 * <p>It sits at the root of the namespace rather than inside {@code common} on
 * purpose. The implementation classes are one package down and are not part of
 * the supported surface; holding one of them directly is how a consumer ends up
 * coupled to a detail that changes without notice. Nothing here hands one out.</p>
 *
 * <h2>Typical use</h2>
 * <pre>{@code
 * TemplateProvider provider = TemplateProvider.instance;
 * provider.handle(TemplateRequest.of(playerId, TemplateAction.GREET))
 *         .thenAccept(response -> player.sendMessage(response.message()));
 * }</pre>
 */
public final class TemplateProvider implements TemplateService {

    /**
     * The installed provider, or null before a platform has created one.
     *
     * <p>Assigned by the constructor and never cleared. Declared
     * {@code volatile} because it is installed on the main thread while the
     * plugin enables, but read from asynchronous callbacks and, on Folia, from
     * region threads.</p>
     */
    public static volatile TemplateProvider instance;

    /**
     * The service every call is delegated to.
     *
     * <p>Private, and never returned by any method: no module outside this one
     * holds the implementation, which is what makes this class the only way in.</p>
     */
    private final TemplateService service;

    /**
     * Wraps a service and installs the result as {@link #instance}.
     *
     * @param service The service to delegate to.
     */
    public TemplateProvider(TemplateService service) {
        this.service = Objects.requireNonNull(service, "service cannot be null");
        instance = this;
    }

    /**
     * Builds the default service and installs a provider around it.
     *
     * <p>This is the sanctioned way to bring the project up. Call
     * {@link #initialize()} afterwards.</p>
     *
     * @return The installed provider, which is also {@link #instance}.
     */
    public static TemplateProvider create() {
        return new TemplateProvider(new DefaultTemplateService());
    }

    /**
     * Whether a provider has been installed.
     *
     * @return True once a provider exists.
     */
    public static boolean isReady() {
        return instance != null;
    }

    /**
     * Brings the underlying service up.
     *
     * @return A future completing when the service is ready.
     */
    @Override
    public CompletableFuture<Void> initialize() {
        return service.initialize();
    }

    /**
     * Performs one requested action.
     *
     * @param request The action to perform.
     * @return A future carrying the server's answer.
     */
    @Override
    public CompletableFuture<TemplateResponse> handle(TemplateRequest request) {
        return service.handle(request);
    }

    /**
     * Reads a player's greeting from memory.
     *
     * @param playerId The player to look up.
     * @return The greeting, or empty when the player has none.
     */
    @Override
    public Optional<String> greetingFor(UUID playerId) {
        return service.greetingFor(playerId);
    }

    /**
     * Shuts the underlying service down.
     */
    @Override
    public void shutdown() {
        service.shutdown();
    }
}
