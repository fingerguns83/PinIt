package net.fg83.pinit.completers;

import net.fg83.pinit.PinIt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MakePinCompleter implements TabCompleter {
    final PinIt plugin;
    public MakePinCompleter(PinIt plugin) { this.plugin = plugin; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is not "makepin" or "makeserverpin", return null if true.
        if (!command.getName().equalsIgnoreCase("makepin") && !command.getName().equalsIgnoreCase("makeserverpin")){
            return null;
        }

        // Get the player
        Player player = (Player) sender;

        // Initialize the output list
        List<String> output = new ArrayList<>();


        switch (args.length) {
            // Case 1: Tab completion for the first argument (world name).
            case 1 -> {
                List<String> worlds;
                worlds = plugin.getWorldNames(false);
                for (String world : worlds) {
                    if (world.replace(" ", "-").toLowerCase().startsWith(args[0].toLowerCase())) {
                        output.add(world.replace(" ", "-"));
                    }
                }
            }

            // Case 2: Tab completion for the second argument (X-coordinate).
            case 2 -> {
                output.add("~");
                output.add(String.valueOf(player.getLocation().getBlockX()));
                output.add(String.valueOf(player.getTargetBlock(null, 7).getLocation().getBlockX()));
            }

            // Case 3: Tab completion for the third argument (Y-coordinate).
            case 3 -> {
                output.add("~");
                output.add(String.valueOf(player.getLocation().getBlockY()));
                output.add(String.valueOf(player.getTargetBlock(null, 7).getLocation().getBlockY()));
            }

            // Case 4: Tab completion for the fourth argument (Z-coordinate).
            case 4 -> {
                output.add("~");
                output.add(String.valueOf(player.getLocation().getBlockZ()));
                output.add(String.valueOf(player.getTargetBlock(null, 7).getLocation().getBlockZ()));
            }

            // Case 5: Tab completion for the fifth argument (tag).
            case 5 -> {
                List<String> tags = new ArrayList<>();
                if (command.getName().equalsIgnoreCase("makeserverpin")) {
                    tags.addAll(plugin.getAllTags(null));
                } else {
                    tags.addAll(plugin.getAllTags(player));
                }
                if (!tags.contains("uncategorized")) {
                    tags.add("uncategorized");
                }
                for (String tag : tags) {
                    if (tag.toLowerCase().startsWith(args[4].toLowerCase())) {
                        output.add(tag);
                    }
                }
            }
            default -> {
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