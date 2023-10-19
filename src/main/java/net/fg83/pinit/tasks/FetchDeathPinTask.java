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
