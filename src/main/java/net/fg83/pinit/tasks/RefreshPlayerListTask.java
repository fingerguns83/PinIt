package net.fg83.pinit.tasks;

import net.fg83.pinit.PinIt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RefreshPlayerListTask implements Runnable {
    PinIt plugin;

    public RefreshPlayerListTask(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public void run() {

        try {
            Statement fetchPlayerListStatement = plugin.connection.createStatement();
            ResultSet playerListResult = fetchPlayerListStatement.executeQuery("SELECT * FROM players ORDER BY LOWER(name)");
            plugin.playersByName.clear();
            plugin.playersById.clear();
            while (playerListResult.next()){
                String playerName = playerListResult.getString("name");
                String playerId = playerListResult.getString("player_id");
                plugin.playersById.put(playerId, playerName);
                plugin.playersByName.put(playerName, playerId);
            }
        }
        catch (SQLException e){
            plugin.getLogger().info("Player List Refresh Error. " + e.getMessage());
        }

    }
}
