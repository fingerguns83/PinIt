package net.fg83.pinit.completers;

import net.fg83.pinit.PinIt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PinItCompleter implements TabCompleter {
    final PinIt plugin;
    public PinItCompleter(PinIt plugin){this.plugin = plugin;}
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is not "pinit" or "serverpinit", return null if true;
        if (!command.getName().equalsIgnoreCase("pinit") && !command.getName().equalsIgnoreCase("serverpinit")){
            return null;
        }

        // Get the player
        Player player = (Player) sender;

        // Initialize the output list
        List<String> output = new ArrayList<>();

        // Check if there is only one argument.
        if (args.length == 1){
            // Initialize a list to store tags.
            List<String> tags;

            // Check if the command is "serverpinit".
            if (command.getName().equalsIgnoreCase("serverpinit")){
                // Get all tags for the server.
                tags = plugin.getAllTags(null);
            }
            else {
                // Get all tags for the player.
                tags = plugin.getAllTags(player);
            }

            // Add "uncategorized" to the list if it's not already present.
            if (!tags.contains("uncategorized")){
                tags.add("uncategorized");
            }

            // Iterate through tags and add those that start with the provided argument to the output list.
            for (String tag : tags){
                if (tag.toLowerCase().startsWith(args[0].toLowerCase())) {
                    output.add(tag);
                }
            }
        }
        return output;
    }
}

/*
Copyright (C) 2023 fingerguns83

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
