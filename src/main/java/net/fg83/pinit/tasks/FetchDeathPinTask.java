package net.fg83.pinit.tasks;

import net.fg83.pinit.Pin;
import net.fg83.pinit.PinIt;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FetchDeathPinTask implements Runnable{
    final PinIt plugin;
    final Player player;
    final String targetPlayerId;

    public FetchDeathPinTask(PinIt plugin, Player player, String targetPlayerId){
        this.plugin = plugin;
        this.player = player;
        this.targetPlayerId = targetPlayerId;
    }
    @Override
    public void run() {
        try {
            // Construct SQL query to retrieve death pins for the target player
            String input = "SELECT * FROM death_pins WHERE player_id = '" + targetPlayerId + "'";
            Statement statement = plugin.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(input);

            // Check if there are no matching death pins
            if (!resultSet.next()) {
                plugin.sendPinItMessage(player, "No pins match this query.", false);
            } else {

                // C
                if (!player.getUniqueId().toString().equalsIgnoreCase(targetPlayerId)){
                    Statement getPlayerNameStatement = plugin.connection.createStatement();
                    ResultSet playerNameResult = getPlayerNameStatement.executeQuery("SELECT * FROM players WHERE player_id='" + targetPlayerId.replace("-", "") + "'");
                    playerNameResult.next();
                    String playerName = playerNameResult.getString("name");
                    String message = playerName + "'s last death:";
                    plugin.sendPinItMessage(player, message, false);
                }

                // Create a Pin object from the result set and send the death message
                Pin pin = new Pin(resultSet, null, plugin, true);
                pin.sendDeathMessage(player);
            }
            statement.close();
        } catch (SQLException e) {
            // Log any SQLException that may occur
            plugin.getLogger().info(e.getMessage());
        }
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