package net.fg83.pinit.tasks;

import net.fg83.pinit.Pin;
import net.fg83.pinit.PinIt;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MakePinTask implements Runnable {
    final PinIt plugin;
    final Player player;
    final Pin pin;

    public MakePinTask(PinIt plugin, Player player, Pin pin){
        this.plugin = plugin;
        this.player = player;
        this.pin = pin;
    }
    @Override
    public void run() {

        // Check if the pin storage is successful
        if (pin.store()) {
            if (pin.getDeathPin()){
                List<String> emphatics = new ArrayList<>();
                emphatics.add("Whoopsie doodle! ");
                emphatics.add("Oh, fiddlesticks! ");
                emphatics.add("Uh-oh spaghetti-o! ");
                emphatics.add("RIP in pepperoni. ");
                emphatics.add("Gosh golly gee! ");
                String message = emphatics.get(new Random().nextInt(emphatics.size())) + "Looks like you died, friend.";

                plugin.sendPinItMessage(player, message, false);

                plugin.sendPinItMessage(player, "Here's where it happened. Make sure to save this pin in case you die on the way back.", false);
                // Send a death message to the player's screen
                pin.sendDeathMessage(player);
                return;
            }
            // Send success message to the player with the created pin name
            TextComponent successMessage = new TextComponent();
            TextComponent pinNameMessage = new TextComponent("[" + pin.getName().trim() + "]");

            // Send success message to the player with the created pin name with applicable color
            // Then update the relevant tag list
            if (pin.getGlobal()) {
                pinNameMessage.setColor(ChatColor.GOLD);
                plugin.serverTags.refresh();
            }
            else {
                pinNameMessage.setColor(ChatColor.DARK_AQUA);
                plugin.updatePlayerTags(player);
            }

            successMessage.addExtra(pinNameMessage);
            successMessage.addExtra(new TextComponent(" created successfully!"));
            successMessage.setBold(false);
            plugin.sendPinItMessage(player, successMessage);
        }
        else {
            if (pin.getDeathPin()){
                // If storing fails, log an error message with details about the death location
                plugin.getLogger().info("Error storing death message for \"" + player.getName() + "\"." +
                        "Player died at " + player.getLocation().getBlockX() + ", " +
                        player.getLocation().getBlockY() + ", " +
                        player.getLocation().getBlockZ() + " in " +
                        player.getLocation().getWorld().getName() + " (" + player.getLocation().getWorld().getUID() + ")");
                return;
            }
            // If pin storage fails, send an error message to the player
            plugin.sendPinItMessage(player, "Could not create pin [" + pin.getName().trim() + "]", true);
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