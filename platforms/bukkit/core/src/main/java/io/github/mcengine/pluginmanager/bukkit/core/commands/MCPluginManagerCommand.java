package io.github.mcengine.pluginmanager.bukkit.core.commands;

import io.github.mcengine.pluginmanager.api.manager.DesiredState;
import io.github.mcengine.pluginmanager.bukkit.core.manager.ManagerRuntime;
import io.github.mcengine.pluginmanager.bukkit.core.scheduler.Schedulers;
import io.github.mcengine.pluginmanager.common.manager.ManagerOutcome;
import io.github.mcengine.pluginmanager.common.manager.ManagerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

/**
 * Handles {@code /mcpluginmanager} and its alias {@code /mcpm}.
 *
 * <p>Every subcommand that touches the network or the disk hands the work to
 * {@link Schedulers} and answers when it finishes. None of them block the thread
 * the command arrived on — which on Folia is a region thread, and stalling one
 * of those stalls a slice of the world.</p>
 */
public final class MCPluginManagerCommand implements CommandExecutor, TabCompleter {

    /** Everything a caller needs the permission for. */
    private static final String PERMISSION = "mcpluginmanager.admin";

    private static final List<String> SUBCOMMANDS =
        List.of("status", "servers", "check", "apply", "report", "register", "pending");

    private final ManagerRuntime runtime;

    public MCPluginManagerCommand(ManagerRuntime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Managing plugins is an operator action, and it is available from the
        // console as well -- an unattended server has no player to run it.
        if (!sender.hasPermission(PERMISSION) && !sender.isOp()) {
            sender.sendMessage("You do not have permission to manage plugins.");
            return true;
        }

        String subcommand = args.length == 0 ? "status" : args[0].toLowerCase(Locale.ROOT);

        switch (subcommand) {
            case "status" -> status(sender);
            case "servers" -> servers(sender);
            case "check" -> async(sender, () -> check(sender));
            case "apply" -> async(sender, () -> apply(sender));
            case "report" -> async(sender, () -> report(sender));
            case "pending" -> async(sender, () -> pending(sender));
            case "register" -> {
                if (args.length < 2) {
                    sender.sendMessage("Usage: /" + label + " register <name>");
                    return true;
                }
                String name = String.join(" ", List.of(args).subList(1, args.length));
                async(sender, () -> register(sender, name));
            }
            default -> sender.sendMessage(
                "Usage: /" + label + " <" + String.join("|", SUBCOMMANDS) + ">");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(
        CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String subcommand : SUBCOMMANDS) {
            if (subcommand.startsWith(prefix)) {
                matches.add(subcommand);
            }
        }
        return matches;
    }

    /** Runs work off the calling thread, reporting a failure rather than throwing into it. */
    private void async(CommandSender sender, Runnable work) {
        Schedulers.get().runAsync(() -> {
            try {
                work.run();
            } catch (RuntimeException failure) {
                sender.sendMessage("That failed: " + failure.getMessage());
            }
        });
    }

    private void status(CommandSender sender) {
        var config = runtime.config();
        sender.sendMessage("MCPluginManager on " + ManagerRuntime.platform() + ".");
        sender.sendMessage(config.serverId().isBlank()
            ? "This server is not registered. Run the register subcommand."
            : "Registered as " + config.serverId() + ".");
        sender.sendMessage(config.servers().size() + " central server(s), polling every "
            + config.pollSeconds() + "s, auto-apply "
            + (config.autoApply() ? "on" : "off") + ".");
    }

    private void servers(CommandSender sender) {
        if (runtime.config().servers().isEmpty()) {
            sender.sendMessage("No central server is configured.");
            return;
        }
        // describe() rather than the record's toString(), which would print the
        // token.
        runtime.config().servers().forEach(server -> sender.sendMessage("- " + server.describe()));
    }

    private void check(CommandSender sender) {
        forEachService(sender, service -> {
            DesiredState state = service.check();
            if (state.isEmpty()) {
                sender.sendMessage("Nothing to do.");
                return;
            }
            sender.sendMessage(state.changes().size() + " change(s) pending:");
            state.changes().forEach(change -> sender.sendMessage("- " + change.describe()));
        });
    }

    private void apply(CommandSender sender) {
        forEachService(sender, service -> {
            DesiredState state = service.check();
            if (state.isEmpty()) {
                sender.sendMessage("Nothing to do.");
                return;
            }
            for (ManagerOutcome outcome : service.apply(state)) {
                sender.sendMessage(outcome.message());
            }
            sender.sendMessage("Restart the server to finish applying these.");
        });
    }

    private void report(CommandSender sender) {
        forEachService(sender, service -> {
            int count = service.report(
                ManagerRuntime.platform(),
                org.bukkit.Bukkit.getBukkitVersion().split("-")[0],
                "0.0.0");
            sender.sendMessage("Reported " + count + " installed plugin(s).");
        });
    }

    private void pending(CommandSender sender) {
        forEachService(sender, service -> {
            List<String> pending = service.staging().readPendingDeletions();
            if (pending.isEmpty()) {
                sender.sendMessage("Nothing is waiting to be removed.");
                return;
            }
            sender.sendMessage("Removed when the server stops: " + String.join(", ", pending));
        });
    }

    /**
     * Registers with the first configured central server and prints what to save.
     *
     * <p>It prints rather than writes. Saving would mean {@code saveConfig()},
     * which drops every comment in {@code config.yml} — and in this plugin's
     * config the comments are most of what the file is for. Two values pasted by
     * hand is the smaller cost.</p>
     */
    private void register(CommandSender sender, String name) {
        if (!runtime.config().serverId().isBlank()) {
            sender.sendMessage("This server is already registered as "
                + runtime.config().serverId() + ".");
            return;
        }
        if (runtime.services().isEmpty()) {
            sender.sendMessage("No central server is configured.");
            return;
        }

        ManagerService service = runtime.services().get(0);
        try {
            String key = service.register(name);
            sender.sendMessage("Registered with " + service.server().describe() + ".");
            sender.sendMessage("Put these in config.yml and restart:");
            sender.sendMessage("  server.key: '" + key + "'");
            sender.sendMessage("The panel shows the matching server.id. The key is shown once.");
        } catch (IOException failure) {
            sender.sendMessage("Could not register: " + failure.getMessage());
        }
    }

    /** Runs an action against every configured service, reporting each failure. */
    private void forEachService(CommandSender sender, ServiceAction action) {
        if (runtime.services().isEmpty()) {
            sender.sendMessage("No central server is configured.");
            return;
        }
        for (ManagerService service : runtime.services()) {
            try {
                action.run(service);
            } catch (IOException failure) {
                sender.sendMessage("A central server could not be reached: " + failure.getMessage());
            }
        }
    }

    /** An action that talks to a central server. */
    @FunctionalInterface
    private interface ServiceAction {
        void run(ManagerService service) throws IOException;
    }
}
