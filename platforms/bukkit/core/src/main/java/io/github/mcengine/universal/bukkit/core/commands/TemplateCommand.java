package io.github.mcengine.universal.bukkit.core.commands;

import io.github.mcengine.universal.TemplateProvider;
import io.github.mcengine.universal.api.TemplateAction;
import io.github.mcengine.universal.api.TemplateRequest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles {@code /template}, the one command this template ships with.
 *
 * <p>It reaches the implementation only through {@link TemplateProvider}, which
 * is the rule every platform class here follows.</p>
 */
public final class TemplateCommand implements CommandExecutor {

    /**
     * Runs a subcommand.
     *
     * @param sender Who ran the command.
     * @param command The command itself.
     * @param label The alias used.
     * @param args The arguments after the command.
     * @return Always true; usage is reported by this method rather than by Bukkit.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        if (!TemplateProvider.isReady()) {
            player.sendMessage("The plugin is still starting up. Try again in a moment.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("Usage: /" + label + " <ping|greet [text]>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "ping" -> send(player, TemplateRequest.of(player.getUniqueId(), TemplateAction.PING));
            case "greet" -> send(player, new TemplateRequest(player.getUniqueId(), TemplateAction.GREET,
                String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))));
            default -> player.sendMessage("Unknown subcommand. Usage: /" + label + " <ping|greet [text]>");
        }
        return true;
    }

    /**
     * Sends a request and delivers the answer to the player.
     *
     * <p>The reply is scheduled back onto the thread that owns the player rather
     * than sent from whichever thread completed the future. That matters on
     * Folia, where touching a player from the wrong region thread is an error,
     * and costs nothing on Spigot and Paper.</p>
     *
     * @param player The player who ran the command.
     * @param request The request to perform.
     */
    private void send(Player player, TemplateRequest request) {
        TemplateProvider.instance.handle(request).thenAccept(response ->
            io.github.mcengine.universal.bukkit.core.scheduler.Schedulers.runForEntity(
                player,
                () -> player.sendMessage(response.message()),
                () -> {
                    // The player left before the answer arrived. Nothing to do.
                }));
    }
}
