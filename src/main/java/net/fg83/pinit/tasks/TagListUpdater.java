package net.fg83.pinit.tasks;

import net.fg83.pinit.PinIt;
import net.fg83.pinit.TagList;
import org.bukkit.entity.Player;

public class TagListUpdater implements Runnable {

    final PinIt plugin;

    public TagListUpdater(PinIt plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.printDebug("Updating tag lists...");
        for (Player player : plugin.getServer().getOnlinePlayers()){
            if (plugin.playerTagLists.containsKey(player)){
                plugin.playerTagLists.get(player).refresh();
            }
            else {
                plugin.playerTagLists.put(player, new TagList(player, plugin));
            }
        }
        plugin.serverTags.refresh();
    }
}
