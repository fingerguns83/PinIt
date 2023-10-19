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

/*
Copyright (C) 2023 fingerguns83

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/