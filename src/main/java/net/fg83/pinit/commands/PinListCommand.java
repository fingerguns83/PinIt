package net.fg83.pinit.commands;

import net.fg83.pinit.Pin;
import net.fg83.pinit.PinIt;
import net.fg83.pinit.PinList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Player;

public class PinListCommand implements CommandExecutor {
    PinIt plugin;
    public PinListCommand(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)){
            return false;
        }
        Player player = (Player) sender;

        PinList pinList = new PinList(plugin, null);
        boolean validWorld = false;
        boolean isServer = false;
        final String[] pinWorld = {null};
        int pageNo = 1;

        switch (args.length){
            case 3:
                try {
                    pageNo = Integer.parseInt(args[2]);
                }
                catch (NumberFormatException e){
                    plugin.sendPinItMessage(player, "Invalid command! Usage: \"/listpins [world] [server|me] [page]", true);
                }
            case 2:
                if (!args[1].equalsIgnoreCase("#all")){
                    for (World world : plugin.getServer().getWorlds()){
                        if (plugin.getNamespacedWorld(world.getName(), true).equalsIgnoreCase(args[1])){
                            pinWorld[0] = world.getName();
                            validWorld = true;
                            break;
                        }
                    }
                    if (!validWorld){
                        plugin.sendPinItMessage(player, "Invalid world selection. Try using the tab completes.", true);
                        return false;
                    }
                }
            case 1:
                if (args[0].equalsIgnoreCase("server")){
                    isServer = true;
                }
                else if (!args[0].equalsIgnoreCase("me")){
                    plugin.sendPinItMessage(player, "Invalid command! Usage: \"/listpins [world] [server|me] [page]", true);
                }
            case 0:
                break;
            default:
                plugin.sendPinItMessage(player, "Invalid command! Usage: \"/listpins [true|false] [world] [page]", true);
                return false;
        }

        boolean finalIsServer = isServer;
        int finalPageNo = pageNo;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            TextComponent headerMessage;
            if (finalIsServer){
                headerMessage = new TextComponent("----------Server Pins----------");
                pinList.populate(finalPageNo, pinWorld[0], null);
            }
            else {
                headerMessage = new TextComponent("----------Your Pins----------");
                pinList.populate(finalPageNo, pinWorld[0], player);
            }

            if (pinList.getPins() == null || pinList.getPins().isEmpty()){
                plugin.sendPinItMessage(player, "Looks like there aren't any pins here that match your criteria.", false);
            }
            else {
                headerMessage.setBold(true);
                headerMessage.setColor(ChatColor.AQUA);
                player.spigot().sendMessage(headerMessage);


                for (Pin pin : pinList.getPins()){
                    pin.sendMessage(player, plugin, player.hasPermission("pinit.server.delete"));
                }

                // Make controls
                if (pinList.hasNext() || pinList.hasPrev()){
                    TextComponent controls = new TextComponent();

                    if (pinWorld[0] == null){
                        pinWorld[0] = "#all";
                    }

                    // Make Previous Button (if applicable)
                    if (pinList.hasPrev()){
                        TextComponent prev = new TextComponent("<--");
                        prev.setColor(ChatColor.DARK_AQUA);
                        prev.setBold(true);
                        prev.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/listpins " + finalIsServer + " " + pinWorld[0] + " " + pinList.getPrev()));
                        controls.addExtra(prev);
                    }

                    // Make Page # Display
                    TextComponent current = new TextComponent(" Page " + pinList.getPage() + " ");
                    current.setColor(ChatColor.WHITE);
                    current.setBold(false);
                    controls.addExtra(current);

                    // Make Next Button (if applicable)
                    if (pinList.hasNext()){
                        TextComponent next = new TextComponent("-->");
                        next.setColor(ChatColor.DARK_AQUA);
                        next.setBold(true);
                        next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/listpins " + finalIsServer + " " + pinWorld[0] + " " + pinList.getNext()));
                        controls.addExtra(next);
                    }

                    // Send controls
                    player.spigot().sendMessage(controls);
                }
            }
        });
        return true;
    }
}
