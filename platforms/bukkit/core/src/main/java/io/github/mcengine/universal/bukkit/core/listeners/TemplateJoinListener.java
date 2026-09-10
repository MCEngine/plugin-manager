package io.github.mcengine.universal.bukkit.core.listeners;

import io.github.mcengine.universal.TemplateProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Greets a player who has set a greeting.
 *
 * <p>Reads through {@link TemplateProvider#greetingFor}, which answers from
 * memory and is therefore safe to call directly on the join event rather than
 * scheduled off-thread.</p>
 */
public final class TemplateJoinListener implements Listener {

    /**
     * Sends the player their stored greeting, if they have one.
     *
     * @param event The join event.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!TemplateProvider.isReady()) {
            return;
        }
        TemplateProvider.instance.greetingFor(event.getPlayer().getUniqueId())
            .ifPresent(greeting -> event.getPlayer().sendMessage(greeting));
    }
}
