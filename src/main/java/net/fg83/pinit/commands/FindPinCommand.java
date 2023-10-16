package net.fg83.pinit.commands;

import net.fg83.pinit.PinIt;
import net.fg83.pinit.tasks.FindPinTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FindPinCommand implements CommandExecutor {
    final PinIt plugin;
    public FindPinCommand(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is not "findpin", return false if it isn't
        if (!command.getName().equalsIgnoreCase("findpin")){
            return false;
        }

        // Check if the command was issued by a player
        if (!(sender instanceof Player)){
            return false;
        }

        // Get the player
        Player player = (Player) sender;

        List<String> parsedArgs = PinIt.parseArguments(args);

        // Check if the command has no arguments
        if (parsedArgs.size() < 1){
            return false;
        }

        if (parsedArgs.size() > 2){
            plugin.sendPinItMessage(player, "Whoa there, sport. You've got too many arguments.", true);
            return false;
        }

        // Check if the first argument is "help"
        if (args[0].equalsIgnoreCase("help")){
            return false;
        }

        int pageNo;
        if (parsedArgs.size() == 1){
            pageNo = 1;
        }
        else {
            try {
                pageNo = Integer.parseInt(parsedArgs.get(1));
                if (pageNo < 1){
                    pageNo = 1;
                }
            }
            catch (NumberFormatException e){
                plugin.getLogger().info(e.getMessage());
                return false;
            }
        }




        // Run the Find Pin Task asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new FindPinTask(plugin, player, parsedArgs.get(0), pageNo));
        return true;

    }
}
