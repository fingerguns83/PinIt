package net.fg83.pinit.tasks;

import net.fg83.pinit.Pin;
import net.fg83.pinit.PinIt;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FindPinTask implements Runnable{
    final PinIt plugin;
    final Player player;
    final String query;
    final int pageNo;
    final int offset;
    boolean hasNext = false;
    boolean hasPrev = false;

    public FindPinTask(PinIt plugin, Player player, String query, int pageNo){
        this.plugin = plugin;
        this.player = player;
        this.query = query.toLowerCase().trim();
        this.pageNo = pageNo;
        this.offset = (pageNo - 1) * 6;
    }
    @Override
    public void run() {

        // Initialize a list to store Pin objects retrieved from the database.
        List<Pin> matches = new ArrayList<>();

        try {
            // Iterate twice: once for global pins and once for player-specific pins.
            for (int i = 0; i < 2; i++) {
                // Prepare a SQL statement for pin search based on the iteration.
                PreparedStatement pinSearchStatement;
                String table;
                if (i == 0) {
                    table = "global_pins";
                } else {
                    table = "player" + player.getUniqueId().toString().replace("-", "");
                }
                pinSearchStatement = plugin.connection.prepareStatement(
                        "SELECT * FROM " + table + " WHERE LOWER(name) LIKE ?");

                // Set the query parameter for the SQL statement, then execute the query.
                pinSearchStatement.setString(1, "%" + query + "%");
                ResultSet serverResults = pinSearchStatement.executeQuery();

                // Process each row in the ResultSet to create Pin objects.
                while (serverResults.next()) {
                    int pinId = serverResults.getInt("id");
                    String name = serverResults.getString("name");
                    String location_world = serverResults.getString("location_world");
                    int locationX = serverResults.getInt("locationX");
                    int locationY = serverResults.getInt("locationY");
                    int locationZ = serverResults.getInt("locationZ");
                    String category = serverResults.getString("category");

                    String worldName = plugin.worldById.get(location_world).getSafeName();

                    Pin pin;
                    if (i == 0) {
                        pin = new Pin(name, category, worldName, locationX, locationY, locationZ, plugin, null, false);
                    } else {
                        pin = new Pin(name, category, worldName, locationX, locationY, locationZ, plugin, player, false);
                    }
                    pin.setPinId(pinId);
                    matches.add(pin);
                }
                pinSearchStatement.close();
            }
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }

        // Create and send the header message.
        sendHeaderMessage(player);

        // Check if there are no pins matching the criteria.
        if (matches.isEmpty()) {
            plugin.sendPinItMessage(player, "No matches.", false);
            return;
        }

        // Check if the requested page is beyond the number of matches.
        else if (offset > matches.size()) {
            plugin.sendPinItMessage(player, "Page not found. Just use the GUI.", true);
            return;
        }

        // Send pins until either a) 6 pins have been sent, or b) the list of matches has been exhausted
        else {
            for (int i = 0; i < 6; i++) {
                int index = i + offset;
                if (index + 1 <= matches.size()) {
                    matches.get(index).sendMessage(player);
                }
            }
        }

        // Check if there's a previous page
        if (pageNo > 1){
            this.hasPrev = true;
        }

        // Check if there's a next page
        if (matches.size() > pageNo * 6){
            this.hasNext = true;
        }

        if (hasNext || hasPrev) {
            TextComponent controls = new TextComponent();


            // Make Previous Button (if applicable)
            if (hasPrev) {
                controls.addExtra(makePreviousButton(query, pageNo));
            }

            // Make Page # Display
            TextComponent current = new TextComponent(" Page " + pageNo + " ");
            current.setColor(ChatColor.WHITE);
            current.setBold(false);
            controls.addExtra(current);

            // Make Next Button (if applicable)
            if (hasNext) {
                controls.addExtra(makeNextButton(query, pageNo));
            }

            // Send controls
            player.spigot().sendMessage(controls);
        }
    }
    private void sendHeaderMessage(Player player){
        TextComponent serverTitle = new TextComponent("----------Found  Pins----------");
        serverTitle.setBold(true);
        serverTitle.setColor(ChatColor.AQUA);
        player.spigot().sendMessage(serverTitle);
    }
    private TextComponent makePreviousButton(String query, int pageNo){
        int prevPage = pageNo - 1;
        String queryString = (query.contains(" "))
            ? "\"" + query + "\""
            : query;


        TextComponent prev = new TextComponent("<--");
        prev.setColor(ChatColor.DARK_AQUA);
        prev.setBold(true);
        prev.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/findpin " + queryString + " " + prevPage));
        return prev;
    }
    private TextComponent makeNextButton(String query, int pageNo){
        int nextPage = pageNo + 1;
        String queryString = (query.contains(" "))
                ? "\"" + query + "\""
                : query;


        TextComponent next = new TextComponent("-->");
        next.setColor(ChatColor.DARK_AQUA);
        next.setBold(true);
        next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/findpin " + queryString + " " + nextPage));
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