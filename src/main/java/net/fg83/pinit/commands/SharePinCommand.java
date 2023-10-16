package net.fg83.pinit.commands;

import net.fg83.pinit.PinIt;

import net.fg83.pinit.tasks.SharePinTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SharePinCommand implements CommandExecutor {
    final PinIt plugin;
    public SharePinCommand(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is "sharepin"
        if (!command.getName().equalsIgnoreCase("sharepin")){
            return false;
        }

        // Check if the sender is a player
        if (!(sender instanceof Player)){
            return false;
        }

        // Retrieve the player who executed the command
        Player player = (Player) sender;

        // Check if the number of arguments is not equal to 2
        // This will also handle /sharepin help
        if (args.length != 2){
            return false;
        }

        // Parse pinId from the first argument
        int pinId;
        try {
            pinId = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e){
            // Inform the player about the invalid pinId
            plugin.sendPinItMessage(player, "Invalid pinId. Try using the share button from \"/pinlist\".", true);
            return false;
        }

        // Check if the pinId is less than 1
        if (pinId < 1){
            // Inform the player about the invalid pinId
            plugin.sendPinItMessage(player, "Invalid pinId. Try using the share button from \"/pinlist\".", true);
            return false;
        }

        // Find the target player by name
        Player target = plugin.getServer().getOnlinePlayers().stream().filter(onlinePlayer -> onlinePlayer.getName().equalsIgnoreCase(args[1])).findFirst().orElse(null);

        // Check if the target player is not found
        if (target == null) {
            plugin.sendPinItMessage(player, "Player is either offline or does not exist.", true);
            return false;
        }

        // Run SharePinTask asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new SharePinTask(plugin, player, target, pinId));
        return true;
    }
}
