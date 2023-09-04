package net.fg83.pinit.completers;

import net.fg83.pinit.PinIt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SharePinCompleter implements TabCompleter {
    PinIt plugin;
    public SharePinCompleter(PinIt plugin) { this.plugin = plugin; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> output = new ArrayList<>();
        if (args.length == 1){
            for (Player player : plugin.getServer().getOnlinePlayers()){
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    output.add(player.getName());
                }
            }
        }
        return output;
    }
}
