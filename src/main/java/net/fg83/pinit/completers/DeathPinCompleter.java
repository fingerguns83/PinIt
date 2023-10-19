package net.fg83.pinit.completers;

import net.fg83.pinit.PinIt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeathPinCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> output = new ArrayList<>();
        // Check if the command is not "deathpin", return null if true
        if (!command.getName().equalsIgnoreCase("deathpin")){
            return null;
        }

        // Get the player
        Player player = (Player) sender;

        if (args.length > 1){
            // If there are more than one argument, return an empty list
            return new ArrayList<>();
        }
        else if (args.length == 1 && player.hasPermission("pinit.server")){
            // If there is one argument and the player has the required permission, return null for default tab completion
            return null;
        }
        else {
            // For any other case, return an empty list
            return new ArrayList<>();
        }
    }
}
