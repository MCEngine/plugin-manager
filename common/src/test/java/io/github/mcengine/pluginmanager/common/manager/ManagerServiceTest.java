package io.github.mcengine.pluginmanager.common.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import io.github.mcengine.pluginmanager.api.manager.CentralServer;
import io.github.mcengine.pluginmanager.api.manager.DesiredState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The whole flow, against a real HTTP server and a real directory.
 *
 * <p>What is worth testing here is the sequencing and what ends up on disk, and
 * neither is visible through a mock.</p>
 */
class ManagerServiceTest {

    private HttpServer server;
    private CentralServer central;
    private final List<String> outcomesReported = new ArrayList<>();

    @TempDir
    Path plugins;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        central = new CentralServer(
            "test",
            URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
            "mcpm_token",
            0);

        server.createContext("/api/v1/fleet/servers/s1/events", exchange -> {
            outcomesReported.add(
                new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.createContext("/api/v1/fleet/servers/s1/plugins", exchange -> {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private void serveDesired(String json) {
        server.createContext("/api/v1/fleet/servers/s1/desired", exchange -> {
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
    }

    private void serveJar(byte[] bytes) {
        server.createContext("/download", exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "application/java-archive");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }

    private ManagerService service() {
        return new ManagerService(new HttpManagerClient(central), plugins, "s1");
    }

    @Test
    @DisplayName("stages an update into plugins/update/ for the server to apply on restart")
    void stagesAnUpdate() throws IOException {
        // The installed jar, so the staged one takes its file name and replaces
        // it rather than loading alongside it.
        PluginInventoryTest.pluginJar(plugins, "EssentialsX-2.19.0.jar", "Essentials", "2.19.0");

        byte[] newJar = "the new jar".getBytes(StandardCharsets.UTF_8);
        String digest = Checksums.sha256(new java.io.ByteArrayInputStream(newJar));
        serveJar(newJar);
        serveDesired("""
            {"actions":[{"action":"update","plugin_id":"Essentials","product_id":"01P",
            "from_version":"2.19.0","to_version":"2.20.1","download_url":"/download",
            "sha256":"%s","size_bytes":11}],"poll_after_seconds":300}""".formatted(digest));

        ManagerService service = service();
        List<ManagerOutcome> outcomes = service.apply(service.check());

        assertEquals(1, outcomes.size());
        assertTrue(outcomes.get(0).staged());

        // Bukkit cannot unload a plugin, so nothing was replaced in place.
        Path staged = plugins.resolve("update").resolve("EssentialsX-2.19.0.jar");
        assertTrue(Files.exists(staged), "the jar is staged under the installed file's name");
        assertEquals(digest, Checksums.sha256(staged));
        assertTrue(Files.exists(plugins.resolve("EssentialsX-2.19.0.jar")), "the old jar is untouched");
        assertTrue(outcomes.get(0).message().contains("restart"));
    }

    @Test
    @DisplayName("names a newly installed jar after the plugin, since nothing is there yet")
    void namesANewInstall() throws IOException {
        byte[] jar = "a brand new jar".getBytes(StandardCharsets.UTF_8);
        String digest = Checksums.sha256(new java.io.ByteArrayInputStream(jar));
        serveJar(jar);
        serveDesired("""
            {"actions":[{"action":"install","plugin_id":"Vault","product_id":"01P",
            "to_version":"1.7.3","download_url":"/download","sha256":"%s","size_bytes":15}],
            "poll_after_seconds":300}""".formatted(digest));

        ManagerService service = service();
        service.apply(service.check());

        assertTrue(Files.exists(plugins.resolve("update").resolve("Vault.jar")));
    }

    @Test
    @DisplayName("writes nothing when the download does not match its checksum")
    void refusesTamperedJar() throws IOException {
        PluginInventoryTest.pluginJar(plugins, "essentials.jar", "Essentials", "2.19.0");
        serveJar("tampered".getBytes(StandardCharsets.UTF_8));
        serveDesired("""
            {"actions":[{"action":"update","plugin_id":"Essentials","product_id":"01P",
            "from_version":"2.19.0","to_version":"2.20.1","download_url":"/download",
            "sha256":"%s","size_bytes":8}],"poll_after_seconds":300}"""
            .formatted("b".repeat(64)));

        ManagerService service = service();
        List<ManagerOutcome> outcomes = service.apply(service.check());

        assertFalse(outcomes.get(0).staged());
        assertTrue(outcomes.get(0).message().contains("checksum"));
        assertFalse(Files.exists(plugins.resolve("update").resolve("essentials.jar")));
        // And the server is told, so the panel shows what the operator sees.
        assertTrue(outcomesReported.stream().anyMatch(body -> body.contains("\"failed\"")));
    }

    @Test
    @DisplayName("keeps going after one change fails")
    void oneFailureDoesNotStopTheRest() throws IOException {
        byte[] good = "a good jar".getBytes(StandardCharsets.UTF_8);
        String digest = Checksums.sha256(new java.io.ByteArrayInputStream(good));
        serveJar(good);
        serveDesired("""
            {"actions":[
              {"action":"update","plugin_id":"Broken","product_id":"01P","from_version":"1.0.0",
               "to_version":"1.1.0","download_url":"/download","sha256":"%s","size_bytes":10},
              {"action":"install","plugin_id":"Working","product_id":"01Q","to_version":"1.0.0",
               "download_url":"/download","sha256":"%s","size_bytes":10}
            ],"poll_after_seconds":300}""".formatted("c".repeat(64), digest));

        ManagerService service = service();
        List<ManagerOutcome> outcomes = service.apply(service.check());

        // Four plugins on a server should still get their updates when the
        // fifth has a problem.
        assertEquals(2, outcomes.size());
        assertFalse(outcomes.get(0).staged());
        assertTrue(outcomes.get(1).staged());
        assertTrue(Files.exists(plugins.resolve("update").resolve("Working.jar")));
    }

    @Test
    @DisplayName("records a delete and performs it at shutdown, not while running")
    void deletesAtShutdown() throws IOException {
        Path jar = PluginInventoryTest.pluginJar(plugins, "old.jar", "OldPlugin", "1.0.0");
        serveDesired("""
            {"actions":[{"action":"delete","plugin_id":"OldPlugin"}],"poll_after_seconds":300}""");

        ManagerService service = service();
        List<ManagerOutcome> outcomes = service.apply(service.check());

        assertTrue(outcomes.get(0).staged());
        assertTrue(Files.exists(jar), "a loaded jar cannot be deleted on Windows");
        assertTrue(outcomes.get(0).message().contains("server stops"));

        assertEquals(List.of("OldPlugin"), service.staging().applyPendingDeletions());
        assertFalse(Files.exists(jar));
    }

    @Test
    @DisplayName("reports the installed inventory with checksums")
    void reportsInventory() throws IOException {
        PluginInventoryTest.pluginJar(plugins, "a.jar", "Alpha", "1.0.0");
        PluginInventoryTest.pluginJar(plugins, "b.jar", "Beta", "2.0.0");

        assertEquals(2, service().report("paper", "1.21.11", "0.0.0"));
    }

    @Test
    @DisplayName("does nothing at all when there is nothing to do")
    void emptyStateIsANoOp() throws IOException {
        serveDesired("{\"actions\":[],\"poll_after_seconds\":300}");

        ManagerService service = service();
        DesiredState state = service.check();

        assertTrue(state.isEmpty());
        assertTrue(service.apply(state).isEmpty());
        assertFalse(Files.exists(plugins.resolve("update")));
    }
}
