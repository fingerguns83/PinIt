package net.fg83.pinit.tasks;

import net.fg83.pinit.PinIt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CheckWarpTask implements Runnable {
    final PinIt plugin;
    final Player player;
    public CheckWarpTask(PinIt plugin, Player player){
        this.plugin = plugin;
        this.player = player;
    }
    @Override
    public void run() {
        try {
            // Construct the SQL query to check for warps associated with the player
            String input = "SELECT * FROM warps WHERE player_id = '" + player.getUniqueId() + "'";

            // Create a statement to execute the SQL query and execute it
            Statement checkWarpStatement = plugin.connection.createStatement();
            ResultSet resultSet = checkWarpStatement.executeQuery(input);

            // Check if the result set has any rows
            if (resultSet.next()){
                // Check if the server associated with the warp is not the current server
                if (!resultSet.getString("server").equalsIgnoreCase(plugin.config.getString("server-name"))){
                    return; // If not, exit the method
                }

                // Retrieve information about the warp
                int warpId = resultSet.getInt("id");
                String locationWorld = resultSet.getString("location_world");
                int locationX = resultSet.getInt("locationX");
                int locationY = resultSet.getInt("locationY");
                int locationZ = resultSet.getInt("locationZ");

                // Find the world object based on the UUID stored in the database
                World pinWorld = null;
                for (World world : plugin.getServer().getWorlds()){
                    if (world.getUID().toString().equalsIgnoreCase(locationWorld)){
                        pinWorld = world;
                        break;
                    }
                }

                // If the world is not found, send an error message and exit the method
                if (pinWorld == null){
                    plugin.sendPinItMessage(player, "Something went wrong. PinIt:960", true);
                    return;
                }

                // Create a location object representing the warp location
                Location pinLocation = new Location(pinWorld, locationX, locationY, locationZ);

                // Create a statement to delete the warp entry from the database and execute it. Then close the statement.
                Statement deleteWarpStatement = plugin.connection.createStatement();
                deleteWarpStatement.executeUpdate("DELETE FROM warps WHERE id = '" + warpId + "'");
                deleteWarpStatement.close();

                // Schedule a task to teleport the player to the warp location synchronously
                Bukkit.getScheduler().runTask(plugin, () -> player.teleport(pinLocation));
            }
            checkWarpStatement.close();
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }
}
