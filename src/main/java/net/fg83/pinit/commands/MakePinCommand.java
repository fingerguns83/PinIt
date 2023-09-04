package net.fg83.pinit.commands;

import net.fg83.pinit.PersonalPin;
import net.fg83.pinit.PinIt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MakePinCommand implements CommandExecutor {
    PinIt plugin;
    public MakePinCommand(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            Player player = (Player) sender;
            if (args.length < 5){
                plugin.sendPinItMessage(player, "Missing info. Usage \"/pinit <world> <x> <y> <z> <name>\"", true);
            }
            else {
                String name = "";
                String pinWorld = "";
                int locationX = player.getLocation().getBlockX();
                int locationY = player.getLocation().getBlockY();
                int locationZ = player.getLocation().getBlockZ();
                for (int i = 0; i < args.length; i++) {
                    try {
                        switch (i){
                            case 0:
                                boolean valid = false;
                                for (World world : plugin.getServer().getWorlds()){
                                    if (plugin.getNamespacedWorld(world.getName(), true).equalsIgnoreCase(args[0])){
                                        pinWorld = world.getName();
                                        valid = true;
                                        break;
                                    }
                                }
                                if (!valid){
                                    plugin.sendPinItMessage(player, "Invalid world selection. Try using the tab completes.", true);
                                    return false;
                                }
                            case 1:
                                if (!args[1].equalsIgnoreCase("~")){
                                    locationX = Integer.parseInt(args[1]);
                                }
                                break;
                            case 2:
                                if (!args[2].equalsIgnoreCase("~")){
                                    locationY = Integer.parseInt(args[2]);
                                }
                                break;
                            case 3:
                                if (!args[3].equalsIgnoreCase("~")){
                                    locationZ = Integer.parseInt(args[3]);
                                }
                                break;
                            default:
                                name = name.concat(" " + args[i]);

                        }
                    }
                    catch (NumberFormatException e){
                        plugin.sendPinItMessage(player, "One of your coordinates is not a number. Usage \"/pinit <world> <x> <y> <z> <name>\"", true);
                        return false;
                    }
                }
                if (name.trim().length() > 24){
                    plugin.sendPinItMessage(player, "Pin names must be 24 characters or less.", true);
                    return false;
                }

                Location location = new Location(plugin.getServer().getWorld(pinWorld), locationX, locationY, locationZ);

                String finalName = name;
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    new PersonalPin(player, finalName.trim(), location, plugin).store();
                    plugin.sendPinItMessage(player, "[" + finalName.trim() + "] created successfully!", false);
                });
            }
            return true;
        }
        return false;
    }
}
