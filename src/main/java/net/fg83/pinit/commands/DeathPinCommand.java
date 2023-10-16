package net.fg83.pinit.commands;

import net.fg83.pinit.PinIt;
import net.fg83.pinit.tasks.FetchDeathPinTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DeathPinCommand implements CommandExecutor {
    final PinIt plugin;

    public DeathPinCommand(PinIt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Check if the command is not "deathpin"
        if (!command.getName().equalsIgnoreCase("deathpin")) {
            return false;
        }

        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            return false;
        }

        // Get the player
        Player player = (Player) sender;

        // Initialize target player variable
        String targetPlayer;

        // Check the number of arguments provided
        if (args.length > 1) {
            return false; // Incorrect command usage
        }
        else if (args.length == 1) {
            // Check if the player has the "pinit.server" permission to query for another player
            if (!player.hasPermission("pinit.server")) {
                return false;
            }
            targetPlayer = plugin.getServer().getOfflinePlayer(args[0].trim()).getUniqueId().toString();
        }
        else {
            // If no argument is provided, target the command sender
            targetPlayer = player.getUniqueId().toString();
        }

        // Run the FetchDeathPinTask asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new FetchDeathPinTask(plugin, player, targetPlayer));

        return true;
    }
}
