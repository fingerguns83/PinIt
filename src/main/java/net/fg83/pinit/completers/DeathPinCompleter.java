package net.fg83.pinit.completers;

import net.fg83.pinit.PinIt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DeathPinCompleter implements TabCompleter {

    PinIt plugin;

    public DeathPinCompleter(PinIt plugin){
        this.plugin = plugin;
    }

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
            // If there is one argument and the player has the required permission, return player list
            for (String targetPlayer : plugin.playersByName.keySet().stream().toList()) {
                if (targetPlayer.startsWith(args[0].toLowerCase())) {
                    output.add(targetPlayer);
                }
            }
            return output;
        }
        else {
            // For any other case, return an empty list
            return new ArrayList<>();
        }
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