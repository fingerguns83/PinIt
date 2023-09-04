package net.fg83.pinit.completers;

import net.fg83.pinit.PinIt;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PinListCompleter implements TabCompleter {
    PinIt plugin;
    public PinListCompleter(PinIt plugin) { this.plugin = plugin; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        List<String> output = new ArrayList<>();

        switch (args.length){
            case 1:
                output.add("server");
                output.add("me");
                break;
            case 2:
                for (World world : plugin.getServer().getWorlds()){
                    output.add(plugin.getNamespacedWorld(world.getName(), true));
                }
                output.add("#all");
                break;
            default:
        }
        return output;
    }
}
