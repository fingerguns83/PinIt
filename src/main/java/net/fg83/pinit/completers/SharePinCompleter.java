package net.fg83.pinit.completers;

import net.fg83.pinit.PinIt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class SharePinCompleter implements TabCompleter {
    PinIt plugin;

    public SharePinCompleter(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> output = new ArrayList<>();
        if (args.length > 1){
            for (String target : plugin.playersByName.keySet().stream().toList()) {
                if (target.toLowerCase().startsWith(args[1].toLowerCase())) {
                    output.add(target);
                }
            }
        }

        return output;
    }
}
