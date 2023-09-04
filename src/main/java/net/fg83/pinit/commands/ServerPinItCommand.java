package net.fg83.pinit.commands;

import net.fg83.pinit.PersonalPin;
import net.fg83.pinit.Pin;
import net.fg83.pinit.PinIt;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServerPinItCommand implements CommandExecutor {
    PinIt plugin;

    public ServerPinItCommand(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            return false;
        }
        Player player = (Player) sender;

        String name = "";
        for (int i = 0; i < args.length; i++){
            name = name.concat(" " + args[i]);
        }
        name = name.trim();

        if (name.length() > 24){
            plugin.sendPinItMessage(player, "Pin names must be 24 characters or less.", true);
            return false;
        }

        String finalName = name;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            new Pin(finalName, player.getLocation(), plugin).store();
            plugin.sendPinItMessage(player, "[" + finalName + "] created successfully!", false);
        });

        return true;
    }
}
