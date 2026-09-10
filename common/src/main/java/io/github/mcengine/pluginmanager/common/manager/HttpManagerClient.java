package io.github.mcengine.pluginmanager.common.manager;

import io.github.mcengine.pluginmanager.api.manager.CentralServer;
import io.github.mcengine.pluginmanager.api.manager.ChecksumMismatchException;
import io.github.mcengine.pluginmanager.api.manager.DesiredChange;
import io.github.mcengine.pluginmanager.api.manager.DesiredState;
import io.github.mcengine.pluginmanager.api.manager.InstalledPlugin;
import io.github.mcengine.pluginmanager.api.manager.ManagerClient;
import io.github.mcengine.pluginmanager.api.manager.PluginAction;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * The {@link ManagerClient} over {@code java.net.http}.
 *
 * <p>No dependency: the JDK's HTTP client is enough, and a Bukkit plugin that
 * shades one competes with whatever the server already has on its classpath.</p>
 */
public final class HttpManagerClient implements ManagerClient {

    /** Nothing this plugin downloads is plausibly larger than this. */
    public static final long MAX_DOWNLOAD_BYTES = 256L * 1024 * 1024;

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private final CentralServer server;
    private final HttpClient http;

    public HttpManagerClient(CentralServer server) {
        this(server, HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            // Never follow a redirect. A 3xx away from the configured server is
            // a request to fetch code from somewhere this plugin was not told to
            // trust, and the whole model rests on it only ever fetching from a
            // server it was configured with.
            .followRedirects(HttpClient.Redirect.NEVER)
            .build());
    }

    /** For tests, which supply a client backed by a local server. */
    public HttpManagerClient(CentralServer server, HttpClient http) {
        this.server = server;
        this.http = http;
    }

    @Override
    public CentralServer server() {
        return server;
    }

    @Override
    public String register(String name) throws IOException {
        String body = "{" + Json.quote("name") + ":" + Json.quote(name) + "}";
        Object payload = Json.parse(post("/api/v1/fleet/servers", body));
        String key = Json.string(payload, "server_key");
        if (key == null) {
            throw new IOException("The server did not return a server key.");
        }
        return key;
    }

    @Override
    public void report(
        String serverId,
        List<InstalledPlugin> installed,
        String platform,
        String mcVersion,
        String agentVersion
    ) throws IOException {
        StringJoiner plugins = new StringJoiner(",", "[", "]");
        for (InstalledPlugin plugin : installed) {
            StringJoiner entry = new StringJoiner(",", "{", "}");
            entry.add(Json.quote("pluginId") + ":" + Json.quote(plugin.pluginId()));
            entry.add(Json.quote("version") + ":"
                + (plugin.version() == null ? "null" : Json.quote(plugin.version())));
            if (plugin.sha256() != null) {
                entry.add(Json.quote("sha256") + ":" + Json.quote(plugin.sha256()));
            }
            plugins.add(entry.toString());
        }

        StringJoiner body = new StringJoiner(",", "{", "}");
        body.add(Json.quote("platform") + ":" + Json.quote(platform));
        body.add(Json.quote("mcVersion") + ":" + Json.quote(mcVersion));
        body.add(Json.quote("agentVersion") + ":" + Json.quote(agentVersion));
        body.add(Json.quote("plugins") + ":" + plugins);

        post("/api/v1/fleet/servers/" + encode(serverId) + "/plugins", body.toString());
    }

    @Override
    public DesiredState desired(String serverId) throws IOException {
        String response = get("/api/v1/fleet/servers/" + encode(serverId) + "/desired");
        Object payload = Json.parse(response);

        List<DesiredChange> changes = new ArrayList<>();
        for (Object entry : Json.array(payload, "actions")) {
            DesiredChange change = readChange(entry);
            // A newer server may describe work this version does not understand.
            // Skipping one is correct; failing the whole poll is not.
            if (change != null) {
                changes.add(change);
            }
        }

        int poll = (int) Json.number(payload, DesiredState.MINIMUM_POLL_SECONDS, "poll_after_seconds");
        return new DesiredState(changes, poll);
    }

    private DesiredChange readChange(Object entry) {
        PluginAction action = PluginAction.fromWire(Json.string(entry, "action"));
        String pluginId = Json.string(entry, "plugin_id");
        if (action == null || pluginId == null) {
            return null;
        }

        try {
            if (action == PluginAction.DELETE) {
                return new DesiredChange(action, pluginId, null, null, null, null, null, -1);
            }
            String url = Json.string(entry, "download_url");
            return new DesiredChange(
                action,
                pluginId,
                Json.string(entry, "product_id"),
                Json.string(entry, "from_version"),
                Json.string(entry, "to_version"),
                url == null ? null : server.resolve(url),
                Json.string(entry, "sha256"),
                Json.number(entry, -1, "size_bytes"));
        } catch (RuntimeException malformed) {
            // A change that fails its own invariants -- no checksum, a plugin id
            // carrying a separator -- is dropped rather than acted on.
            return null;
        }
    }

    @Override
    public void download(DesiredChange change, Path target) throws IOException {
        if (change.downloadUrl() == null || change.sha256() == null) {
            throw new IOException("That change carries nothing to download.");
        }

        HttpRequest request = HttpRequest.newBuilder(change.downloadUrl())
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + server.token())
            .header("Accept", "application/java-archive")
            .GET()
            .build();

        Path directory = target.getParent();
        if (directory != null) {
            Files.createDirectories(directory);
        }

        // Downloaded beside the target, verified, and only then moved into
        // place. A jar that fails its checksum must never exist at a path
        // anything might load from, not even briefly.
        Path staging = Files.createTempFile(
            directory == null ? target.toAbsolutePath().getParent() : directory,
            ".mcpm-", ".part");

        try {
            HttpResponse<InputStream> response =
                send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() / 100 != 2) {
                throw new IOException("Downloading " + change.pluginId() + " failed with HTTP "
                    + response.statusCode());
            }

            long declared = response.headers().firstValueAsLong("content-length").orElse(-1);
            if (declared > MAX_DOWNLOAD_BYTES) {
                throw new IOException("Refusing a download of " + declared + " bytes.");
            }

            long written;
            try (InputStream body = response.body()) {
                written = Files.copy(body, staging, StandardCopyOption.REPLACE_EXISTING);
            }
            if (written > MAX_DOWNLOAD_BYTES) {
                throw new IOException("Refusing a download of " + written + " bytes.");
            }

            String actual = Checksums.sha256(staging);
            if (!Checksums.matches(actual, change.sha256())) {
                throw new ChecksumMismatchException(change.pluginId(), change.sha256(), actual);
            }

            Files.move(staging, target,
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(staging);
        }
    }

    @Override
    public void reportOutcome(String serverId, String pluginId, String state, String error)
        throws IOException {
        StringJoiner body = new StringJoiner(",", "{", "}");
        body.add(Json.quote("pluginId") + ":" + Json.quote(pluginId));
        body.add(Json.quote("state") + ":" + Json.quote(state));
        body.add(Json.quote("error") + ":" + (error == null ? "null" : Json.quote(error)));
        post("/api/v1/fleet/servers/" + encode(serverId) + "/events", body.toString());
    }

    private String get(String path) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(server.resolve(path))
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + server.token())
            .header("Accept", "application/json")
            .GET()
            .build();
        return body(send(request, HttpResponse.BodyHandlers.ofString()), path);
    }

    private String post(String path, String json) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(server.resolve(path))
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + server.token())
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        return body(send(request, HttpResponse.BodyHandlers.ofString()), path);
    }

    private <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> handler)
        throws IOException {
        try {
            return http.send(request, handler);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while talking to " + server.describe(), interrupted);
        }
    }

    private String body(HttpResponse<String> response, String path) throws IOException {
        int status = response.statusCode();
        if (status / 100 == 2) {
            return response.body() == null || response.body().isBlank() ? "{}" : response.body();
        }

        // The service returns one error envelope for every failure, so the
        // message a person sees comes from it rather than from a status code.
        String message = null;
        try {
            message = Json.string(Json.parse(response.body()), "error", "message");
        } catch (RuntimeException ignored) {
            // Not an envelope. The status alone will have to do.
        }
        throw new IOException(server.describe() + " answered " + status + " for " + path
            + (message == null ? "" : ": " + message));
    }

    private static String encode(String segment) {
        return URI.create("http://x/" + segment).getPath().substring(1);
    }

    /** Exposed for tests that assert what was sent. */
    public Map<String, String> headersFor() {
        return Map.of("Authorization", "Bearer " + server.token());
    }
}
