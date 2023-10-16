package net.fg83.pinit.commands;

import net.fg83.pinit.PinIt;
import net.fg83.pinit.tasks.DeletePinTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeletePinCommand implements CommandExecutor {

    final PinIt plugin;
    public DeletePinCommand(PinIt plugin) { this.plugin = plugin; }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is not "deletepin" or "deleteserverpin", return falase if true
        if (!command.getName().equalsIgnoreCase("deletepin") && !command.getName().equalsIgnoreCase("deleteserverpin")){
            return false;
        }

        // Check if the sender is a player
        if (!(sender instanceof Player)){
            return false;
        }

        // Get the player
        Player player = (Player) sender;

        // If there is less than one argument, return false
        if (args.length != 1){
            return false;
        }

        // Check if the first argument is "help"
        if (args[0].equalsIgnoreCase("help")) {
            return false;
        }

        int pinId;
        try {
            pinId = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e){
            plugin.sendPinItMessage(player, "pinId NAN! Just use the GUI.", true);
            return false;
        }
        if (pinId < 1){
            plugin.sendPinItMessage(player, "Invalid argument! Just use the GUI.", true);
            return false;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new DeletePinTask(plugin, command, player, pinId));

        return true;
    }
}
