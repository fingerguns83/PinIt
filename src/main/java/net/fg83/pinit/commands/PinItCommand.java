package net.fg83.pinit.commands;

import net.fg83.pinit.PinIt;
import net.fg83.pinit.Pin;

import net.fg83.pinit.tasks.MakePinTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PinItCommand implements CommandExecutor {
    final PinIt plugin;

    public PinItCommand(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is either "pinit" or "serverpinit"
        if (!command.getName().equalsIgnoreCase("pinit") && !command.getName().equalsIgnoreCase("serverpinit")){
            return false;
        }

        // Check if the sender is a player
        if (!(sender instanceof Player)){
            return false;
        }

        // Get the player
        Player player = (Player) sender;

        // Parse command arguments
        List<String> parsedArgs = PinIt.parseArguments(args);

        // Check if there are no arguments
        if (parsedArgs.isEmpty()){
            plugin.sendPinItMessage(player, "You need to give your pin a name and category.", true);
            return false;
        }

        // Check if there is only one argument, and if it is "help"
        if (parsedArgs.size() == 1){
            if (parsedArgs.get(0).equalsIgnoreCase("help")){
                return false;
            }
            if (parsedArgs.get(0).equalsIgnoreCase("about") && command.getName().equalsIgnoreCase("pinit")){
                TextComponent aboutMessage = new TextComponent("PinIt v" + plugin.versionString + " by fingerguns83. ");
                aboutMessage.setColor(ChatColor.WHITE);
                aboutMessage.setBold(false);

                TextComponent websiteMessage = new TextComponent("https://fg83.net");
                websiteMessage.setUnderlined(true);
                websiteMessage.setBold(false);
                websiteMessage.setColor(ChatColor.GREEN);

                aboutMessage.addExtra(websiteMessage);

                plugin.sendPinItMessage(player, aboutMessage);
                return true;
            }
            plugin.sendPinItMessage(player, "You're missing a name for your pin.", true);
            return false;
        }

        // Check if there are more than two arguments
        if (parsedArgs.size() > 2){
            plugin.sendPinItMessage(player, "Whoa there, sport. You've got too many arguments.", true);
            return false;
        }

        // Extract tag and name from arguments
        String tag = parsedArgs.get(0).trim();
        String name = parsedArgs.get(1).trim();

        // Validate pin name and tag
        if (!plugin.validatePinName(name, player) || !plugin.validatePinTag(tag, player)){
            return false;
        }

        // Create a new Pin object based on the command
        Pin pin;
        if (command.getName().equalsIgnoreCase("pinit")){
            pin = new Pin(name, tag, player.getLocation(), plugin, player, false);
        }
        else {
            pin = new Pin(name, tag, player.getLocation(), plugin, null, false);
        }

        // Run the pin creation task asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new MakePinTask(plugin, player, pin));

        return true;
    }
}
