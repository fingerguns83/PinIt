package net.fg83.pinit.commands;

import net.fg83.pinit.PinIt;

import net.fg83.pinit.tasks.BuildPinListTask;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PinListCommand implements CommandExecutor {
    final PinIt plugin;
    public PinListCommand(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is "pinlist"
        if (!command.getName().equalsIgnoreCase("pinlist")){
            return false;
        }

        // Check if the sender is a player
        if (!(sender instanceof Player)){
            return false;
        }

        // Get the player
        Player player = (Player) sender;

        // Initialize variables with default values
        String pinWorld = null;
        Player pinPlayer = player;
        String tag = null;
        int pageNo = 1;

        // Process command arguments
        switch (args.length){
            case 4:
                try {
                    // Attempt to parse the fourth argument as pageNo
                    pageNo = Integer.parseInt(args[3]);

                    // If argument is less than page 1, set to page 1
                    if (pageNo < 1){
                        pageNo = 1;
                    }
                }
                catch (NumberFormatException e){
                    return false;
                }
            case 3:
                // Check if the third argument is not "#all" and set it as the tag
                if (!args[2].equalsIgnoreCase("#all")){
                    tag = args[2];
                }
                else {
                    tag = "#all";
                }
            case 2:
                // Check if the second argument is not "@all" or "@local" and set it as pinWorld
                if (!args[1].equalsIgnoreCase("@all") && !args[1].equalsIgnoreCase("@local")){
                    pinWorld = args[1];
                }
                else {
                    // Check if the second argument is "@local" and set pinWorld based on player's world
                    if (args[1].equalsIgnoreCase("@local")){
                        pinWorld = plugin.getPinItWorldName(player.getWorld().getUID().toString(), false);
                    }
                }
            case 1:
                // Check if the first argument is "help" and return usage if it is
                if (args[0].equalsIgnoreCase("help")){
                    return false;
                }

                // Check if the first argument is "server" and set pinPlayer to null
                if (args[0].equalsIgnoreCase("server")){
                    pinPlayer = null;
                }
            case 0:
                break;
            default:
                // Return false if there are more than 4 arguments
                plugin.sendPinItMessage(player, "Invalid command!", true);
                return false;
        }

        // Validate pinWorld if not null
        if (pinWorld != null){
            if (!plugin.validateWorldName(pinWorld)){
                plugin.sendPinItMessage(player, "World name invalid. Try using the tab-completes.", true);
                return false;
            }
        }

        // Run BuildPinListTask asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new BuildPinListTask(plugin, player, pinPlayer, pageNo, pinWorld, tag));
        return true;
    }
}
