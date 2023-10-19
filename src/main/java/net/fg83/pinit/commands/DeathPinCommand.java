package net.fg83.pinit.commands;

import net.fg83.pinit.PinIt;
import net.fg83.pinit.tasks.FetchDeathPinTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeathPinCommand implements CommandExecutor {
    final PinIt plugin;

    public DeathPinCommand(PinIt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is not "deathpin"
        if (!command.getName().equalsIgnoreCase("deathpin")) {
            return false;
        }

        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            return false;
        }

        // Get the player
        Player player = (Player) sender;

        // Initialize target player variable
        String targetPlayer;

        // Check the number of arguments provided
        if (args.length > 1) {
            return false; // Incorrect command usage
        }
        else if (args.length == 1) {
            // Check if the player has the "pinit.server.deathpins" permission to query for another player
            if (!player.hasPermission("pinit.server.deathpins")) {
                plugin.sendPinItMessage(player, "You don't have permission to view another player's death pin.", true);
                return false;
            }
            targetPlayer = plugin.getServer().getOfflinePlayer(args[0].trim()).getUniqueId().toString();
        }
        else {
            // If no argument is provided, target the command sender
            targetPlayer = player.getUniqueId().toString();
        }

        // Run the FetchDeathPinTask asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new FetchDeathPinTask(plugin, player, targetPlayer));

        return true;
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