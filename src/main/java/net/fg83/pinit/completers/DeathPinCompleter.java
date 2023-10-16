package net.fg83.pinit.completers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DeathPinCompleter implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
