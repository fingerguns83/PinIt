package net.fg83.pinit.completers;

import net.fg83.pinit.PinIt;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MakePinCompleter implements TabCompleter {
    PinIt plugin;
    public MakePinCompleter(PinIt plugin) { this.plugin = plugin; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        List<String> output = new ArrayList<>();
        switch (args.length){
            case 1:
                for (World world : plugin.getServer().getWorlds()){
                    if (plugin.getNamespacedWorld(world.getName(), true).toLowerCase().startsWith(args[0].toLowerCase())) {
                        output.add(plugin.getNamespacedWorld(world.getName(), true));
                    }
                }
                break;
            case 2:
                output.add("~");
                output.add(String.valueOf(player.getLocation().getBlockX()));
                output.add(String.valueOf(player.getTargetBlock(null, 7).getLocation().getBlockX()));
                break;
            case 3:
                output.add("~");
                output.add(String.valueOf(player.getLocation().getBlockY()));
                output.add(String.valueOf(player.getTargetBlock(null, 7).getLocation().getBlockY()));
                break;
            case 4:
                output.add("~");
                output.add(String.valueOf(player.getLocation().getBlockZ()));
                output.add(String.valueOf(player.getTargetBlock(null, 7).getLocation().getBlockZ()));
                break;
            default:
        }
        return output;
    }
}
