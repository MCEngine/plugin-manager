package io.github.mcengine.universal;

import io.github.mcengine.universal.api.TemplateAction;
import io.github.mcengine.universal.api.TemplateRequest;
import io.github.mcengine.universal.api.TemplateResponse;
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
class TemplateProviderTest {

    /**
     * The provider under test.
     */
    private TemplateProvider provider;

    /**
     * An arbitrary player.
     */
    private UUID playerId;

    /**
     * Installs a fresh provider and brings it up.
     */
    @BeforeEach
    void setUp() {
        provider = TemplateProvider.create();
        provider.initialize().join();
        playerId = UUID.randomUUID();
    }

    /**
     * Leaves no running service behind for the next test.
     */
    @AfterEach
    void tearDown() {
        provider.shutdown();
        TemplateProvider.instance = null;
    }

    @Test
    @DisplayName("create installs the provider as the singleton instance")
    void createInstallsSingleton() {
        assertSame(provider, TemplateProvider.instance);
        assertTrue(TemplateProvider.isReady());
    }

    @Test
    @DisplayName("a ping is accepted while the service is running")
    void pingIsAcceptedWhileRunning() {
        TemplateResponse response = provider.handle(TemplateRequest.of(playerId, TemplateAction.PING)).join();

        assertTrue(response.accepted());
        assertEquals("pong", response.message());
    }

    @Test
    @DisplayName("a ping is rejected after shutdown, rather than throwing")
    void pingIsRejectedAfterShutdown() {
        provider.shutdown();

        TemplateResponse response = provider.handle(TemplateRequest.of(playerId, TemplateAction.PING)).join();

        assertFalse(response.accepted());
        assertEquals("The service is not running", response.message());
    }

    @Test
    @DisplayName("a greet with a payload stores it and reads back synchronously")
    void greetStoresThePayload() {
        TemplateResponse response = provider
            .handle(new TemplateRequest(playerId, TemplateAction.GREET, "Good evening")).join();

        assertTrue(response.accepted());
        assertEquals("Good evening", response.message());
        assertEquals("Good evening", provider.greetingFor(playerId).orElseThrow());
    }

    @Test
    @DisplayName("a greet with no payload falls back to the default and stores nothing")
    void greetWithoutPayloadFallsBack() {
        TemplateResponse response = provider.handle(TemplateRequest.of(playerId, TemplateAction.GREET)).join();

        assertTrue(response.accepted());
        assertEquals("Hello from the universal template", response.message());
        assertTrue(provider.greetingFor(playerId).isEmpty());
    }

    @Test
    @DisplayName("shutdown clears stored greetings")
    void shutdownClearsState() {
        provider.handle(new TemplateRequest(playerId, TemplateAction.GREET, "Hi")).join();

        provider.shutdown();

        assertTrue(provider.greetingFor(playerId).isEmpty());
    }

    @Test
    @DisplayName("the provider refuses to wrap a null service")
    void refusesNullService() {
        assertThrows(NullPointerException.class, () -> new TemplateProvider(null));
    }
}
