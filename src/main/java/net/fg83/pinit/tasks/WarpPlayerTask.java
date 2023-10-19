package net.fg83.pinit.tasks;

import net.fg83.pinit.Pin;
import net.fg83.pinit.PinIt;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WarpPlayerTask implements Runnable {
    final PinIt plugin;
    final Player player;
    final String table;
    final int pinId;

    public WarpPlayerTask(PinIt plugin, Player player, String table, int pinId){
        this.plugin = plugin;
        this.player = player;
        this.table = table;
        this.pinId = pinId;
    }
    @Override
    public void run() {
        try {
            // Construct the SQL query to retrieve the pin information based on the table and pin ID.
            // Execute the query and store the results
            String input = "SELECT * FROM " + table + " WHERE id = " + pinId;
            Statement getPinStatement = plugin.connection.createStatement();
            ResultSet resultSet = getPinStatement.executeQuery(input);

            // Check if the result set has any rows
            if (resultSet.next()){
                // Initialize a Pin object based on the result set
                Pin pin;

                // Check if the table is for global pins or player-specific pins
                if (table.equalsIgnoreCase("global_pins")){
                    // For global pins, create a Pin object without a player reference
                    pin = new Pin(resultSet, null, plugin, false);
                }
                else {
                    // For player-specific pins, create a Pin object with the player reference
                    pin = new Pin(resultSet, player, plugin, false);
                }

                // Begin the warp process
                buildWarp(player, pin);
            }
            else {
                plugin.printDebug("Could not find target.");
            }
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }

    private void buildWarp(Player player, Pin pin){
        // Trim the server name from the pin...just in case
        String server = pin.getServer().trim();

        // Check if the pin's server matches the current server
        if (server.equalsIgnoreCase(plugin.config.getString("server-name"))){
            // If destination is on current server, find the world object based on the UUID stored in the pin
            World pinWorld = null;
            for (World world : plugin.getServer().getWorlds()){
                if (world.getUID().toString().equalsIgnoreCase(pin.getWorldId())){
                    pinWorld = world;
                    break;
                }
            }

            // If the world is not found, send an error message and exit the method
            if (pinWorld == null){
                plugin.sendPinItMessage(player, "Something went wrong. PinIt:960", true);
                return;
            }

            // Create a location object representing the pin location
            Location pinLocation = new Location(pinWorld, pin.getLocationX(), pin.getLocationY(), pin.getLocationZ());

            // Schedule a task to teleport the player to the pin location
            Bukkit.getScheduler().runTask(plugin, () -> player.teleport(pinLocation));
        }

        else {
            // If the pin is on a different server, create a warp entry in the "warps" table
            int warpId;
            try {
                // Construct the SQL query to insert a new warp entry
                String input = "INSERT INTO warps (player_id, server, location_world, locationX, locationY, locationZ) VALUES ('" +
                        player.getUniqueId() + "', '" +
                        pin.getServer() + "', '" +
                        pin.getWorldId() + "', " +
                        pin.getLocationX() + ", " +
                        pin.getLocationY() + ", " +
                        pin.getLocationZ() + ")";

                // Create a statement to execute the SQL query, and get the generated keys
                Statement createWarpEntryStatement = plugin.connection.createStatement();
                createWarpEntryStatement.executeUpdate(input, Statement.RETURN_GENERATED_KEYS);

                ResultSet generatedKeys = createWarpEntryStatement.getGeneratedKeys();

                // Check if keys were generated
                if (generatedKeys.next()){
                    warpId = generatedKeys.getInt(1);
                }
                else {
                    throw new SQLException("Something went wrong. PinIt:921");
                }

                // Close the statement used for creating the warp entry
                createWarpEntryStatement.close();
            }
            catch (SQLException e){
                plugin.getLogger().info(e.getMessage());
                return;
            }

            // Send a confirmation prompt and schedule a task to remove the warp entry after 20 seconds (400 ticks)
            makeWarpConfirmationMessage(player, pin);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new WarpExpiryTask(plugin, player, pin, warpId), 400);
        }
    }
    public void makeWarpConfirmationMessage(Player player, Pin pin){
        TextComponent warpConfirm = new TextComponent();

        TextComponent leftArrow = new TextComponent("--> ");
        leftArrow.setColor(ChatColor.DARK_GREEN);

        TextComponent mainMessage = new TextComponent("Go to ");
        mainMessage.setItalic(true);
        mainMessage.setColor(ChatColor.GOLD);

        TextComponent locationMessage = new TextComponent("[" + pin.getName() + "]");
        locationMessage.setItalic(false);
        locationMessage.setColor(ChatColor.AQUA);

        TextComponent rightArrow = new TextComponent(" <--");
        rightArrow.setColor(ChatColor.DARK_GREEN);

        warpConfirm.addExtra(leftArrow);
        warpConfirm.addExtra(mainMessage);
        warpConfirm.addExtra(locationMessage);
        warpConfirm.addExtra(rightArrow);

        // When clicked, "/server" command will execute as user, sending them to the target server.
        // When the player joins the target server, plugin completes the warp process.
        // See: "net.fg83.pinit.tasks.CheckWarpTask"
        warpConfirm.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + pin.getServer()));

        player.spigot().sendMessage(warpConfirm);
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