package net.fg83.pinit.completers;

import net.fg83.pinit.PinIt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PinListCompleter implements TabCompleter {
    final PinIt plugin;
    public PinListCompleter(PinIt plugin) { this.plugin = plugin; }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Check if the command is not "pinlist", return null if true
        if (!command.getName().equalsIgnoreCase("pinlist")){
            return null;
        }

        // Get the player;
        Player player = (Player) sender;

        // Initialize the output list
        List<String> output = new ArrayList<>();

        switch (args.length) {
            // Case 1: Tab completion for the first argument (List Type [server/personal]).
            case 1 -> {
                List<String> targets = new ArrayList<>();
                targets.add("server");
                targets.add("me");
                for (String target : targets) {
                    if (target.toLowerCase().startsWith(args[0].toLowerCase())) {
                        output.add(target);
                    }
                }
            }

            // Case 2: Tab completion for the second argument (world).
            case 2 -> {
                List<String> worlds;
                worlds = plugin.getWorldNames(false);
                worlds.add("@local");
                worlds.add("@all");
                for (String world : worlds) {
                    if (world.replace(" ", "-").toLowerCase().startsWith(args[1].toLowerCase())) {
                        output.add(world.replace(" ", "-"));
                    }
                }
            }

            // Case 3: Tab completion for the third argument (tag).
            case 3 -> {
                List<String> tags = new ArrayList<>();
                if (args[0].equalsIgnoreCase("server")) {
                    tags.addAll(plugin.getAllTags(null));
                } else {
                    tags.addAll(plugin.getAllTags(player));
                }
                tags.add("#all");
                for (String tag : tags) {
                    if (tag.toLowerCase().startsWith(args[2].toLowerCase())) {
                        output.add(tag);
                    }
                }
            }
            default -> {
            }
        }
        return output;
    }
}
