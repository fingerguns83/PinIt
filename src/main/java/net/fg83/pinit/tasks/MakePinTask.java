package net.fg83.pinit.tasks;

import net.fg83.pinit.Pin;
import net.fg83.pinit.PinIt;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

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
            // Send success message to the player with the created pin name
            plugin.sendPinItMessage(player, "[" + pin.getName().trim() + "] created successfully!", false);
            TextComponent successMessage = new TextComponent();
            TextComponent pinNameMessage = new TextComponent("[" + pin.getName().trim() + "]");

            // Send success message to the player with the created pin name with applicable color
            // Then update the relevant tag list
            if (pin.getGlobal()) {
                pinNameMessage.setColor(ChatColor.BLUE);
                plugin.serverTags.refresh();
            }
            else {
                pinNameMessage.setColor(ChatColor.GOLD);
                plugin.updatePlayerTags(player);
            }

            successMessage.addExtra(pinNameMessage);
            successMessage.addExtra(new TextComponent(" created successfully!"));
        }
        else {
            // If pin storage fails, send an error message to the player
            plugin.sendPinItMessage(player, "Could not create pin [" + pin.getName().trim() + "]", true);
        }

    }
}
