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