package net.fg83.pinit.completers;

import net.fg83.pinit.PinIt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class SharePinCompleter implements TabCompleter {

    PinIt plugin;

    public SharePinCompleter(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return plugin.playersByName.keySet().stream().toList();
    }
}
