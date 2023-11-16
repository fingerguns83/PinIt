package net.fg83.pinit.tasks;

import net.fg83.pinit.PinIt;
import net.fg83.pinit.PinList;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class BuildPinListTask implements Runnable {
    final PinIt plugin;
    final Player player;
    final Player tablePlayer;
    final int pageNo;
    final String pinWorld;
    final String tag;

    final PinList pinList;

    public BuildPinListTask(PinIt plugin, Player player, Player tablePlayer, int pageNo, String pinWorld, String tag){
        this.plugin = plugin;
        this.player = player;
        this.tablePlayer = tablePlayer;
        this.pageNo = pageNo;
        this.pinWorld = pinWorld;
        this.tag = tag;

        pinList = new PinList(plugin);
    }

    @Override
    public void run() {

        pinList.populate(pageNo, pinWorld, tablePlayer, tag);

        if (pinList.getPins() == null || pinList.getPins().isEmpty()){
            plugin.sendPinItMessage(player, "Looks like there aren't any pins here that match your criteria.", false);
        }
        else {
            TextComponent headerMessage = (tablePlayer == null)
                    ? new TextComponent("----------Server Pins----------")
                    : new TextComponent("-----------Your Pins-----------");

            headerMessage.setBold(true);
            headerMessage.setColor(ChatColor.AQUA);
            player.spigot().sendMessage(headerMessage);

            pinList.getPins().forEach(pin -> pin.sendMessage(player));

            // Make controls
            if (pinList.hasNext() || pinList.hasPrev()) {
                TextComponent controls = new TextComponent();

                String listType = (tablePlayer == null) ? "server" : "me";

                String pagePinWorld = (pinWorld == null) ? "@all" : pinWorld;

                String pagePinTag = (tag == null) ? "#all" : tag;

                // Make Previous Button (if applicable)
                if (pinList.hasPrev()) {
                    controls.addExtra(makePreviousButton(listType, pagePinWorld, pagePinTag));
                }

                // Make Page # Display
                TextComponent current = new TextComponent(" Page " + pinList.getPage() + " ");
                current.setColor(ChatColor.WHITE);
                current.setBold(false);
                controls.addExtra(current);

                // Make Next Button (if applicable)
                if (pinList.hasNext()) {
                    controls.addExtra(makeNextButton(listType, pagePinWorld, pagePinTag));
                }

                // Send controls
                player.spigot().sendMessage(controls);
            }
        }
    }
    private TextComponent makePreviousButton(String listType, String world, String tag){
        TextComponent prev = new TextComponent("<--");
        prev.setColor(ChatColor.YELLOW);
        prev.setBold(true);
        prev.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pinlist " + listType + " " + world + " " + tag + " " + pinList.getPrev()));
        return prev;
    }
    private TextComponent makeNextButton(String listType, String world, String tag){
        TextComponent next = new TextComponent("-->");
        next.setColor(ChatColor.YELLOW);
        next.setBold(true);
        next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pinlist " + listType + " " + world + " " + tag + " " + pinList.getNext()));
        return next;
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