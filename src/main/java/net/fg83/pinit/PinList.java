package net.fg83.pinit;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PinList {
    final PinIt plugin;
    final List<Pin> pins = new ArrayList<>();
    private int page;
    private boolean hasNextPage = false;
    private boolean hasPrevPage = false;

    public PinList(PinIt plugin){
        this.plugin = plugin;
    }
    public List<Pin> getPins() {
        return pins;
    }
    public boolean hasNext(){
        return hasNextPage;
    }
    public boolean hasPrev(){
        return hasPrevPage;
    }
    public int getNext(){
        return page + 1;
    }
    public int getPrev(){
        return page - 1;
    }
    public int getPage() {
        return page;
    }
    private String makeFetchStatement(int pageNo, @Nullable Player player, @Nullable String worldName, @Nullable String tag){
        // Initialize variables for the world and fetch statement
        String world;

        // If worldName is provided, get the corresponding world ID; otherwise, set world to null
        if (worldName != null) {
            world = plugin.getPinItWorldId(worldName);
        }
        else {
            world = null;
        }

        // Initialize the fetch statement
        String fetchStatement;

        // If player is not null, construct a fetch statement for player-specific pins; otherwise, for global pins
        if (player != null) {
            fetchStatement = "SELECT * FROM player" + player.getUniqueId().toString().replace("-", "") + " ";
        }
        else {
            fetchStatement = "SELECT * FROM global_pins ";
        }

        // Add conditions to the fetch statement based on the provided parameters (world and tag)
        if (world != null && tag != null) {
            fetchStatement = fetchStatement.concat("WHERE location_world = ? AND category = ? ");
        }
        else if (world != null) {
            fetchStatement = fetchStatement.concat("WHERE location_world = ? ");
        }
        else if (tag != null) {
            fetchStatement = fetchStatement.concat("WHERE category = ? ");
        }

        // Add the sorting criteria to the fetch statement
        fetchStatement = fetchStatement.concat("ORDER BY LOWER(name), name LIMIT 6");

        // If the page number is greater than 1, calculate the offset and add it to the fetch statement
        if (pageNo > 1) {
            int offset = (pageNo - 1) * 6;
            fetchStatement = fetchStatement.concat(" OFFSET " + offset);
        }

        // Return the constructed fetch statement
        return fetchStatement;
    }

    private int countRecords(@Nullable Player player, @Nullable String world, @Nullable String tag) {
        // Initialize the fetch statement
        String fetchStatement;

        // If player is not null, construct a fetch statement for player-specific pins; otherwise, for global pins
        if (player != null) {
            fetchStatement = "SELECT COUNT(*) FROM player" + player.getUniqueId().toString().replace("-", "") + " ";
        }
        else {
            fetchStatement = "SELECT COUNT(*) FROM global_pins ";
        }

        // Add conditions to the fetch statement based on the provided parameters (world and tag)
        if (tag != null || world != null) {
            fetchStatement = fetchStatement.concat("WHERE ");
        }

        // Add conditions for world and tag if provided
        if (world != null) {
            fetchStatement = fetchStatement.concat("location_world='" + world + "' ");

            // If both world and tag are provided, add an AND condition
            if (tag != null) {
                fetchStatement = fetchStatement.concat("AND ");
            }
        }

        // Add condition for tag if provided
        if (tag != null) {
            fetchStatement = fetchStatement.concat("category='" + tag + "' ");
        }

        // Initialize the record count
        int recordCount = 0;

        try {
            // Create a statement and execute the fetch statement to get the record count
            Statement statement = plugin.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(fetchStatement);

            // If the result set has a next element, retrieve the record count
            if (resultSet.next()) {
                recordCount = resultSet.getInt(1);
            }

            // Close the result set and statement
            resultSet.close();
            statement.close();
        }
        catch (SQLException e) {
            // Log any SQLException that may occur
            plugin.getLogger().info(e.getMessage());
        }

        // Return the total record count
        return recordCount;
    }

    // Populate the pin list based on specified parameters
    public void populate(int pageNo, @Nullable String world, @Nullable Player player, @Nullable String tag) {
        // Log debug information
        plugin.printDebug("Populating Pin List...");

        // Set the current page number
        this.page = pageNo;

        // Check if there is a previous page
        if (pageNo > 1) {
            this.hasPrevPage = true;
        }

        // Construct the fetch statement for retrieving pins
        String fetchStatement = makeFetchStatement(pageNo, player, world, tag);

        // Log the constructed fetch statement
        plugin.getLogger().info(fetchStatement);

        // Count the total number of records with the specified parameters
        int recordCount = countRecords(player, world, tag);

        // Check if there is a next page based on the current page and total record count
        if ((pageNo * 6) < recordCount) {
            hasNextPage = true;
        }

        // Fetch the list of pins with constraints
        try {
            ResultSet resultSet;

            // Prepare and execute a statement based on provided parameters (world and tag)
            if (world != null || tag != null) {
                PreparedStatement statement = plugin.connection.prepareStatement(fetchStatement);

                // Set parameters in the prepared statement
                if (world != null) {
                    world = world.replace("-", " ");
                    world = plugin.getPinItWorldId(world);
                    statement.setString(1, world);

                    // Set tag parameter if provided
                    if (tag != null) {
                        statement.setString(2, tag);
                    }
                }
                else {
                    // Set tag parameter if world is null
                    statement.setString(1, tag);
                }

                // Execute the prepared statement and get the result set
                resultSet = statement.executeQuery();
            }
            else {
                // If no world or tag is specified, create a regular statement and execute it
                Statement statement = plugin.connection.createStatement();
                resultSet = statement.executeQuery(fetchStatement);
            }

            // Iterate through the result set and create Pin objects to populate the list
            while (resultSet.next()) {
                Pin pin;

                // Create Pin object based on player parameter
                if (player != null) {
                    pin = new Pin(resultSet, player, plugin, false);
                }
                else {
                    pin = new Pin(resultSet, null, plugin, false);
                }

                // Add the Pin object to the list
                pins.add(pin);
            }
        }
        catch (SQLException e) {
            // Log any SQLException that may occur
            plugin.getLogger().info(e.getMessage());
        }
    }
}
