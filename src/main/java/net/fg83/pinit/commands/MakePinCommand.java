package net.fg83.pinit.commands;

import net.fg83.pinit.Pin;
import net.fg83.pinit.PinIt;

import net.fg83.pinit.tasks.MakePinTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MakePinCommand implements CommandExecutor {
    final PinIt plugin;
    public MakePinCommand(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is either "makepin" or "makeserverpin"
        if (!command.getName().equalsIgnoreCase("makepin") && !command.getName().equalsIgnoreCase("makeserverpin")) {
            return false;
        }

        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            return false;
        }

        // Get the player
        Player player = (Player) sender;

        // Parse command arguments
        List<String> parsedArgs = PinIt.parseArguments(args);

        // Check if the first argument is "help"
        if (parsedArgs.size() == 1) {
            if (parsedArgs.get(0).equalsIgnoreCase("help")) {
                return false;
            }
        }

        // Check if the number of arguments is less than 6
        if (parsedArgs.size() < 6) {
            plugin.sendPinItMessage(player, "Missing info.", true);
            return false;
        }

        // Check if the number of arguments is more than 6
        if (parsedArgs.size() > 6) {
            plugin.sendPinItMessage(player, "Whoa there, sport. You've got too many arguments.", true);
            return false;
        }

        // Extract information from parsed arguments
        String name = parsedArgs.get(5).trim();
        String tag = parsedArgs.get(4);
        String pinWorld = parsedArgs.get(0);
        int locationX = player.getLocation().getBlockX();
        int locationY = player.getLocation().getBlockY();
        int locationZ = player.getLocation().getBlockZ();

        // Try to parse and set the coordinates from arguments
        try {
            if (!parsedArgs.get(1).equalsIgnoreCase("~")) {
                locationX = Integer.parseInt(args[1]);
            }
            if (!parsedArgs.get(2).equalsIgnoreCase("~")) {
                locationY = Integer.parseInt(args[2]);
            }
            if (!parsedArgs.get(3).equalsIgnoreCase("~")) {
                locationZ = Integer.parseInt(args[3]);
            }
        }
        catch (NumberFormatException e){
            plugin.sendPinItMessage(player, "One of your coordinates is not a number", true);
            return false;
        }

        // Validate pin name and tag
        if (!plugin.validatePinName(name, player) || !plugin.validatePinTag(tag, player)){
            return false;
        }

        // Validate world name if provided
        if (pinWorld != null){
            if (!plugin.validateWorldName(pinWorld)){
                plugin.sendPinItMessage(player, "World name invalid. Try using the tab-completes.", true);
                return false;
            }
        }

        // Create a new Pin object based on the command
        Pin pin;
        if (command.getName().equalsIgnoreCase("makepin")){
            pin = new Pin(name.trim(), tag, pinWorld, locationX, locationY, locationZ, plugin, player, false);
        }
        else {
            pin = new Pin(name.trim(), tag, pinWorld, locationX, locationY, locationZ, plugin, null, false);
        }

        // Run the pin creation task asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new MakePinTask(plugin, player, pin));

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