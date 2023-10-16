package net.fg83.pinit.tasks;

import net.fg83.pinit.PinIt;
import net.fg83.pinit.PinItWorld;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WorldMappingUpdater implements Runnable{
    final PinIt plugin;
    public WorldMappingUpdater(PinIt plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.printDebug("Updating world mappings...");
        try {
            // Create a statement to execute the SQL query to select all rows from the "worlds" table
            Statement statement = plugin.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM worlds");

            // Iterate through the result set
            while (resultSet.next()){
                // Retrieve data for each world from the result set
                String worldId = resultSet.getString("world_id");
                String worldName = resultSet.getString("fancy_name");

                // Create a PinItWorld object based on the retrieved data
                PinItWorld pinItWorld = new PinItWorld(
                        worldId,
                        worldName,
                        resultSet.getString("server")
                );

                // Update the world mappings in the plugin by associating world ID and world name with the PinItWorld object
                plugin.worldById.put(worldId, pinItWorld);
                plugin.worldByName.put(worldName, pinItWorld);
            }
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }
}
