package io.github.mcengine.universal.bukkit.spigotmc;

import io.github.mcengine.universal.bukkit.core.AbstractTemplatePlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the shape Bukkit requires of an entry point.
 *
 * <p>Bukkit instantiates the class named by plugin.yml reflectively, so a class
 * that is abstract, non-public, or not a JavaPlugin fails at server start with a
 * stack trace rather than at build time. These assertions move that failure to
 * the build, where it is cheap.</p>
 */
class TemplateSpigotMCStructureTest {

    /**
     * The entry point under test.
     */
    private static final Class<?> ENTRY_POINT = TemplateSpigotMC.class;

    @Test
    @DisplayName("the entry point is public and concrete, so Bukkit can instantiate it")
    void entryPointIsInstantiable() {
        assertTrue(Modifier.isPublic(ENTRY_POINT.getModifiers()));
        assertFalse(Modifier.isAbstract(ENTRY_POINT.getModifiers()));
    }

    @Test
    @DisplayName("the entry point inherits the shared bootstrap")
    void entryPointExtendsTheSharedBootstrap() {
        assertEquals(AbstractTemplatePlugin.class, ENTRY_POINT.getSuperclass());
    }

    @Test
    @DisplayName("the entry point supplies the platform scheduler and nothing else")
    void entryPointOnlySuppliesItsScheduler() throws NoSuchMethodException {
        Method createScheduler = ENTRY_POINT.getDeclaredMethod("createScheduler");
        assertEquals("io.github.mcengine.universal.bukkit.core.scheduler.PlatformScheduler", createScheduler.getReturnType().getName());

        // Any other declared method means platform-specific logic has started
        // growing here instead of in the core module, where it would be shared.
        assertEquals(1, ENTRY_POINT.getDeclaredMethods().length,
            "The entry point should declare createScheduler only");
    }
}
