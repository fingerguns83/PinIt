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
        //Player target = plugin.getServer().getOnlinePlayers().stream().filter(onlinePlayer -> onlinePlayer.getName().equalsIgnoreCase(args[1])).findFirst().orElse(null);

        // Check if the target player is not found
        if (plugin.playersByName.get(args[1]) == null) {
            plugin.sendPinItMessage(player, "Player not found.", true);
            return false;
        }
        else {
            // Run SharePinTask asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new SharePinTask(plugin, player, args[1], pinId));
        }

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