package io.github.mcengine.pluginmanager.common.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.mcengine.pluginmanager.api.manager.CentralServer;
import io.github.mcengine.pluginmanager.api.manager.ChecksumMismatchException;
import io.github.mcengine.pluginmanager.api.manager.DesiredChange;
import io.github.mcengine.pluginmanager.api.manager.DesiredState;
import io.github.mcengine.pluginmanager.api.manager.InstalledPlugin;
import io.github.mcengine.pluginmanager.api.manager.PluginAction;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Drives the client against a real HTTP server on loopback.
 *
 * <p>A real socket rather than a mocked client, because what is worth testing
 * here is the wire behaviour — the bearer header, the checksum gate, what
 * happens on a redirect — and a mock would assert only that the code calls the
 * methods it calls.</p>
 */
class HttpManagerClientTest {

    private HttpServer server;
    private CentralServer central;
    private HttpManagerClient client;
    private final Map<String, String> lastHeaders = new HashMap<>();
    private final List<String> requests = new ArrayList<>();
    private String lastBody = "";

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        central = new CentralServer(
            "test",
            URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
            "mcpm_testtokenvalue",
            0);
        client = new HttpManagerClient(central);
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private void route(String path, int status, String body) {
        route(path, status, body.getBytes(StandardCharsets.UTF_8), "application/json");
    }

    private void route(String path, int status, byte[] body, String contentType) {
        server.createContext(path, exchange -> {
            record(exchange);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
    }

    private void record(HttpExchange exchange) throws IOException {
        requests.add(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath());
        exchange.getRequestHeaders().forEach((k, v) -> lastHeaders.put(k.toLowerCase(), v.get(0)));
        lastBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("presents the token as a bearer credential on every request")
    void sendsBearerToken() throws IOException {
        route("/api/v1/fleet/servers/s1/desired", 200, "{\"actions\":[],\"poll_after_seconds\":300}");

        client.desired("s1");
        assertEquals("Bearer mcpm_testtokenvalue", lastHeaders.get("authorization"));
    }

    @Test
    @DisplayName("reads the desired state, resolving the download URL against the server")
    void readsDesiredState() throws IOException {
        route("/api/v1/fleet/servers/s1/desired", 200, """
            {"actions":[{"action":"update","plugin_id":"Essentials","product_id":"01P",
            "from_version":"2.19.0","to_version":"2.20.1",
            "download_url":"/api/v1/products/01P/versions/2.20.1/download",
            "sha256":"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
            "size_bytes":4194304}],"poll_after_seconds":600}""");

        DesiredState state = client.desired("s1");
        assertEquals(1, state.changes().size());
        assertEquals(600, state.pollAfterSeconds());

        DesiredChange change = state.changes().get(0);
        assertEquals(PluginAction.UPDATE, change.action());
        assertEquals("Essentials", change.pluginId());
        // A relative URL from the server becomes absolute against the server it
        // came from, and never against anything else.
        assertTrue(change.downloadUrl().toString().startsWith(central.baseUrl().toString()));
    }

    @Test
    @DisplayName("skips an action it does not understand instead of failing the poll")
    void skipsUnknownActions() throws IOException {
        route("/api/v1/fleet/servers/s1/desired", 200, """
            {"actions":[
              {"action":"teleport","plugin_id":"Whatever"},
              {"action":"delete","plugin_id":"OldPlugin"}
            ],"poll_after_seconds":300}""");

        DesiredState state = client.desired("s1");
        // A newer server may describe work this version has never heard of.
        assertEquals(1, state.changes().size());
        assertEquals(PluginAction.DELETE, state.changes().get(0).action());
    }

    @Test
    @DisplayName("drops a change carrying no checksum rather than acting on it")
    void dropsChangeWithoutChecksum() throws IOException {
        route("/api/v1/fleet/servers/s1/desired", 200, """
            {"actions":[{"action":"update","plugin_id":"Essentials","to_version":"1.0.0",
            "download_url":"/x"}],"poll_after_seconds":300}""");

        assertTrue(client.desired("s1").isEmpty());
    }

    @Test
    @DisplayName("drops a change whose plugin id is a path")
    void dropsPathTraversingPluginId() throws IOException {
        route("/api/v1/fleet/servers/s1/desired", 200, """
            {"actions":[{"action":"delete","plugin_id":"../../server.properties"}],
            "poll_after_seconds":300}""");

        assertTrue(client.desired("s1").isEmpty());
    }

    @Test
    @DisplayName("raises the poll interval to the floor when the server asks for less")
    void clampsPollInterval() throws IOException {
        route("/api/v1/fleet/servers/s1/desired", 200,
            "{\"actions\":[],\"poll_after_seconds\":0}");

        // A zero from a misconfigured server would make this a tight loop.
        assertEquals(DesiredState.MINIMUM_POLL_SECONDS, client.desired("s1").pollAfterSeconds());
    }

    @Test
    @DisplayName("reports the whole inventory, including a plugin with no version")
    void reportsInventory() throws IOException {
        route("/api/v1/fleet/servers/s1/plugins", 204, "");

        client.report("s1", List.of(
            new InstalledPlugin("Essentials", "2.19.0", "a".repeat(64)),
            new InstalledPlugin("Nameless", null, null)
        ), "paper", "1.21.11", "0.0.0");

        assertTrue(lastBody.contains("\"pluginId\":\"Essentials\""));
        assertTrue(lastBody.contains("\"version\":null"), lastBody);
        assertTrue(lastBody.contains("\"platform\":\"paper\""));
    }

    @Test
    @DisplayName("downloads a jar and writes it only after the checksum matches")
    void downloadsAndVerifies(@TempDir Path directory) throws IOException {
        byte[] jar = "pretend this is a jar".getBytes(StandardCharsets.UTF_8);
        String digest = Checksums.sha256(new java.io.ByteArrayInputStream(jar));
        route("/download", 200, jar, "application/java-archive");

        Path target = directory.resolve("update").resolve("Essentials.jar");
        client.download(change(digest), target);

        assertTrue(Files.exists(target));
        assertEquals(digest, Checksums.sha256(target));
    }

    @Test
    @DisplayName("writes nothing when the checksum does not match")
    void refusesOnChecksumMismatch(@TempDir Path directory) throws IOException {
        route("/download", 200, "tampered".getBytes(StandardCharsets.UTF_8), "application/java-archive");

        Path target = directory.resolve("Essentials.jar");
        ChecksumMismatchException failure = assertThrows(ChecksumMismatchException.class,
            () -> client.download(change("b".repeat(64)), target));

        // A jar that fails its checksum must never exist at a path anything
        // might load from -- not even briefly.
        assertFalse(Files.exists(target));
        assertEquals("b".repeat(64), failure.expected());
        assertNotNull(failure.actual());
    }

    @Test
    @DisplayName("leaves no partial file behind when the download fails")
    void leavesNoPartialFile(@TempDir Path directory) {
        route("/download", 500, "{\"error\":{\"code\":\"internal_error\",\"message\":\"no\"}}");

        Path target = directory.resolve("Essentials.jar");
        assertThrows(IOException.class, () -> client.download(change("a".repeat(64)), target));

        assertFalse(Files.exists(target));
        try (var entries = Files.list(directory)) {
            assertEquals(0, entries.count(), "a staging file was left behind");
        } catch (IOException cause) {
            throw new AssertionError(cause);
        }
    }

    @Test
    @DisplayName("never follows a redirect away from the configured server")
    void refusesRedirects(@TempDir Path directory) {
        server.createContext("/download", exchange -> {
            exchange.getResponseHeaders().set("Location", "https://evil.example.com/payload.jar");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });

        // The whole model rests on only ever fetching from a server this plugin
        // was configured to trust.
        assertThrows(IOException.class, () -> client.download(
            change("a".repeat(64)), directory.resolve("x.jar")));
    }

    @Test
    @DisplayName("reports the server's own message when a request fails")
    void surfacesServerMessage() {
        route("/api/v1/fleet/servers/s1/desired", 403,
            "{\"error\":{\"code\":\"missing_scope\",\"message\":\"This action needs the fleet:read scope.\"}}");

        IOException failure = assertThrows(IOException.class, () -> client.desired("s1"));
        assertTrue(failure.getMessage().contains("fleet:read"), failure.getMessage());
        assertTrue(failure.getMessage().contains("test"), "the server is named");
    }

    @Test
    @DisplayName("registers and returns the key exactly once")
    void registers() throws IOException {
        route("/api/v1/fleet/servers", 201,
            "{\"id\":\"s1\",\"name\":\"survival\",\"server_key\":\"01SERVERKEY0000000000000000\"}");

        assertEquals("01SERVERKEY0000000000000000", client.register("survival"));
    }

    @Test
    @DisplayName("reports an outcome, including a failure reason")
    void reportsOutcome() throws IOException {
        route("/api/v1/fleet/servers/s1/events", 204, "");

        client.reportOutcome("s1", "Essentials", "failed", "checksum mismatch");
        assertTrue(lastBody.contains("\"state\":\"failed\""));
        assertTrue(lastBody.contains("checksum mismatch"));
    }

    private DesiredChange change(String sha256) {
        return new DesiredChange(
            PluginAction.UPDATE,
            "Essentials",
            "01P",
            "2.19.0",
            "2.20.1",
            central.resolve("/download"),
            sha256,
            -1);
    }
}
