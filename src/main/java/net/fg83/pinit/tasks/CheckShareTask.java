package net.fg83.pinit.tasks;

import de.myzelyam.api.vanish.VanishAPI;
import net.fg83.pinit.Pin;
import net.fg83.pinit.PinIt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CheckShareTask implements Runnable {
    PinIt plugin;

    public CheckShareTask(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public void run() {
        try {
            Statement getSharesStatement = plugin.connection.createStatement();
            ResultSet resultSet = getSharesStatement.executeQuery("SELECT * FROM shares");
            while (resultSet.next()){
                int shareId = resultSet.getInt("id");
                String selectedPlayer = resultSet.getString("player_to");
                int pinId = resultSet.getInt("pin_id");
                String fromPlayerId = resultSet.getString("player_from");
                plugin.printDebug("Found share [" + shareId + "] from \"" + fromPlayerId + "\" to \"" + selectedPlayer + "\"");

                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    plugin.printDebug("Checking player \"" + player.getName() + "\" (" + player.getUniqueId() + ")");
                    if (player.getUniqueId().toString().replace("-", "").equalsIgnoreCase(selectedPlayer)){
                        plugin.printDebug("Player found!");

                        if (plugin.getServer().getPluginManager().isPluginEnabled("PremiumVanish") || plugin.getServer().getPluginManager().isPluginEnabled("SuperVanish")){
                            if (VanishAPI.getInvisiblePlayers().contains(player.getUniqueId())){
                                return;
                            }
                        }


                        fetchPin(pinId, fromPlayerId).sendShareMessage(player);
                        deleteShare(shareId);
                    }
                });
            }
        }
        catch (SQLException e){
            plugin.getLogger().info("Error checking shares. " + e.getMessage());
        }
    }

    public Pin fetchPin(int pinId, String ownerId){
        String ownerName = plugin.playersById.get(ownerId.replace("-", ""));


        String input = "SELECT * from player" + ownerId.replace("-", "") + " WHERE id = ?";
        try {
            PreparedStatement fetchPinStatement = plugin.connection.prepareStatement(input);
            fetchPinStatement.setInt(1, pinId);
            ResultSet resultSet = fetchPinStatement.executeQuery();
            if (resultSet.next()){
                return new Pin(resultSet, ownerName, plugin);
            }
            else {
                return null;
            }
        }
        catch (SQLException e){
            plugin.getLogger().info("Error fetching pin for share. " + e.getMessage());
            return null;
        }
    }
    public void deleteShare(int shareId){
        String input = "DELETE FROM shares WHERE id = ?";
        try {
            PreparedStatement deleteShareStatement = plugin.connection.prepareStatement(input);
            deleteShareStatement.setInt(1, shareId);
            deleteShareStatement.executeUpdate();
        }
        catch (SQLException e){
            plugin.getLogger().info("Error deleting pin share. " + e.getMessage());
        }

    }
}
