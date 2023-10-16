package net.fg83.pinit.commands;

import net.fg83.pinit.PinIt;
import net.fg83.pinit.tasks.WarpPlayerTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PinWarpCommand implements CommandExecutor {
    final PinIt plugin;

    public PinWarpCommand(PinIt plugin){this.plugin = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Check if the command name is not "pinwarp"
        if (!command.getName().equalsIgnoreCase("pinwarp")){
            return false;
        }

        // Check if there are exactly 2 arguments
        if (args.length != 2){
            plugin.printDebug("Not enough args?");
            return false;
        }

        // Retrieve the player who executed the command
        Player player = (Player) sender;

        // Determine the database table based on the first argument
        String table;
        if (args[0].equalsIgnoreCase("server")){
            table = "global_pins";
        }
        else if (args[0].equalsIgnoreCase("me")){
            table = "player" + player.getUniqueId().toString().replace("-", "");
        }
        else {
            plugin.printDebug("Could not parse table.");
            return false;
        }

        int pinId;
        try {
            // Parse the second argument as an integer representing the pin ID
            pinId = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e){
            // Log an error if the second argument is not a valid integer
            plugin.getLogger().info(e.getMessage());
            return false;
        }

        // Schedule a task to prepare the warp asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new WarpPlayerTask(plugin, player, table, pinId));

        return true;
    }
}
