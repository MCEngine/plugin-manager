package io.github.mcengine.pluginmanager;

import io.github.mcengine.pluginmanager.api.MCPluginManagerAction;
import io.github.mcengine.pluginmanager.api.MCPluginManagerRequest;
import io.github.mcengine.pluginmanager.api.MCPluginManagerResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the facade every platform and every third-party consumer goes through.
 */
class MCPluginManagerProviderTest {

    /**
     * The provider under test.
     */
    private MCPluginManagerProvider provider;

    /**
     * An arbitrary player.
     */
    private UUID playerId;

    /**
     * Installs a fresh provider and brings it up.
     */
    @BeforeEach
    void setUp() {
        provider = MCPluginManagerProvider.create();
        provider.initialize().join();
        playerId = UUID.randomUUID();
    }

    /**
     * Leaves no running service behind for the next test.
     */
    @AfterEach
    void tearDown() {
        provider.shutdown();
        MCPluginManagerProvider.instance = null;
    }

    @Test
    @DisplayName("create installs the provider as the singleton instance")
    void createInstallsSingleton() {
        assertSame(provider, MCPluginManagerProvider.instance);
        assertTrue(MCPluginManagerProvider.isReady());
    }

    @Test
    @DisplayName("a ping is accepted while the service is running")
    void pingIsAcceptedWhileRunning() {
        MCPluginManagerResponse response = provider.handle(MCPluginManagerRequest.of(playerId, MCPluginManagerAction.PING)).join();

        assertTrue(response.accepted());
        assertEquals("pong", response.message());
    }

    @Test
    @DisplayName("a ping is rejected after shutdown, rather than throwing")
    void pingIsRejectedAfterShutdown() {
        provider.shutdown();

        MCPluginManagerResponse response = provider.handle(MCPluginManagerRequest.of(playerId, MCPluginManagerAction.PING)).join();

        assertFalse(response.accepted());
        assertEquals("The service is not running", response.message());
    }

    @Test
    @DisplayName("a greet with a payload stores it and reads back synchronously")
    void greetStoresThePayload() {
        MCPluginManagerResponse response = provider
            .handle(new MCPluginManagerRequest(playerId, MCPluginManagerAction.GREET, "Good evening")).join();

        assertTrue(response.accepted());
        assertEquals("Good evening", response.message());
        assertEquals("Good evening", provider.greetingFor(playerId).orElseThrow());
    }

    @Test
    @DisplayName("a greet with no payload falls back to the default and stores nothing")
    void greetWithoutPayloadFallsBack() {
        MCPluginManagerResponse response = provider.handle(MCPluginManagerRequest.of(playerId, MCPluginManagerAction.GREET)).join();

        assertTrue(response.accepted());
        assertEquals("Hello from MCPluginManager", response.message());
        assertTrue(provider.greetingFor(playerId).isEmpty());
    }

    @Test
    @DisplayName("shutdown clears stored greetings")
    void shutdownClearsState() {
        provider.handle(new MCPluginManagerRequest(playerId, MCPluginManagerAction.GREET, "Hi")).join();

        provider.shutdown();

        assertTrue(provider.greetingFor(playerId).isEmpty());
    }

    @Test
    @DisplayName("the provider refuses to wrap a null service")
    void refusesNullService() {
        assertThrows(NullPointerException.class, () -> new MCPluginManagerProvider(null));
    }
}
