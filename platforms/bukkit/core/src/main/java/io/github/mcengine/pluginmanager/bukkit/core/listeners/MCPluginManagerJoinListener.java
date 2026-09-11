package io.github.mcengine.pluginmanager.bukkit.core.listeners;

import io.github.mcengine.pluginmanager.MCPluginManagerProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Greets a player who has set a greeting.
 *
 * <p>Reads through {@link MCPluginManagerProvider#greetingFor}, which answers from
 * memory and is therefore safe to call directly on the join event rather than
 * scheduled off-thread.</p>
 */
public final class MCPluginManagerJoinListener implements Listener {

    /**
     * Sends the player their stored greeting, if they have one.
     *
     * @param event The join event.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!MCPluginManagerProvider.isReady()) {
            return;
        }
        MCPluginManagerProvider.instance.greetingFor(event.getPlayer().getUniqueId())
            .ifPresent(greeting -> event.getPlayer().sendMessage(greeting));
    }
}
