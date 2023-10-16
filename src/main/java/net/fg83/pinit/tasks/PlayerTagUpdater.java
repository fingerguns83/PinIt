package net.fg83.pinit.tasks;

import net.fg83.pinit.PinIt;
import net.fg83.pinit.TagList;
import org.bukkit.entity.Player;

public class PlayerTagUpdater implements Runnable {
    final PinIt plugin;
    final Player player;
    public PlayerTagUpdater(PinIt plugin, Player player){
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        if (plugin.playerTagLists.containsKey(player)){
            plugin.playerTagLists.get(player).refresh();
        }
        else {
            plugin.playerTagLists.put(player, new TagList(player, plugin));
        }
    }
}
