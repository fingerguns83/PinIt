package net.fg83.pinit;

import net.fg83.pinit.PinIt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Updater {
    final PinIt plugin;
    final Map<String, String> worlds = new HashMap<>();
    public Updater(PinIt plugin){
        this.plugin = plugin;

        plugin.getServer().getWorlds().forEach(world -> {
            String worldName = world.getName().trim();
            String worldId = world.getUID().toString();
            worlds.put(worldName, worldId);
        });
    }

    public void update(){
        updateServerTableName();
        updatePlayersTableSchema();
        updateInfoTableSchema();
        updatePins("global_pins");
        getPlayerTableNames().forEach(tableName -> updatePins(tableName));
        setNewVersion();
        pushNewConfig();
        plugin.getLogger().info("Successfully updated to PinIt 2.0! Enjoy!");
    }
    private void updateServerTableName(){
        plugin.getLogger().info("Renaming server table...");
        try {
            Statement serverNameChangeStatement = plugin.connection.createStatement();
            serverNameChangeStatement.executeUpdate("ALTER TABLE server RENAME TO global_pins");
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }
    private void updatePlayersTableSchema(){
        plugin.getLogger().info("Updating players table schema...");
        try {
            Statement playersTableSchemaStatement = plugin.connection.createStatement();
            playersTableSchemaStatement.executeUpdate("ALTER TABLE players RENAME COLUMN uuid TO player_id");
            playersTableSchemaStatement.close();
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }
    private void updateInfoTableSchema(){
        plugin.getLogger().info("Updating info table schema...");
        try {
            Statement removeOldInfoStatement = plugin.connection.createStatement();
            removeOldInfoStatement.executeUpdate("DROP TABLE info");
            removeOldInfoStatement.close();

            Statement infoTableSchemaStatement = plugin.connection.createStatement();
            infoTableSchemaStatement.executeUpdate("CREATE TABLE IF NOT EXISTS info (record TEXT NOT NULL UNIQUE, data TEXT NOT NULL)");
            infoTableSchemaStatement.close();
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }
    private List<String> getPlayerTableNames(){
        List<String> playerTables = new ArrayList<>();
        try {
            Statement getPlayerTablesStatement = plugin.connection.createStatement();
            ResultSet resultSet = getPlayerTablesStatement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'player%'");
            while (resultSet.next()){
                String tableName = resultSet.getString("name");
                if (!tableName.equalsIgnoreCase("players")){
                    playerTables.add(tableName);
                }
            }
            getPlayerTablesStatement.close();
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }

        return playerTables;
    }
    private void updatePins(String tableName){
        plugin.getLogger().info("Updating \"" + tableName + "\" pins...");
        Map<Integer, String> pins = new HashMap<>();
        try {
            Statement getPinsStatement = plugin.connection.createStatement();
            ResultSet resultSet = getPinsStatement.executeQuery("SELECT * FROM " + tableName);
            while (resultSet.next()){
                int pinId = resultSet.getInt("id");
                String worldName = resultSet.getString("location_world");
                pins.put(pinId, worldName);
            }
            getPinsStatement.close();
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
            return;
        }
        pins.forEach((pinId, worldName) -> {
            String worldId = worlds.get(worldName.trim());
            try {
                Statement updatePinStatement = plugin.connection.createStatement();
                updatePinStatement.executeUpdate(
                        "UPDATE " + tableName + " SET location_world='" + worldId.trim() + "' WHERE id=" + pinId);
                updatePinStatement.close();
            }
            catch (SQLException e){
                plugin.getLogger().info(e.getMessage());
            }
        });
    }
    private void setNewVersion(){
        plugin.getLogger().info("Updating DB version entry...");
        try {
            Statement updateDbVersionStatement = plugin.connection.createStatement();
            updateDbVersionStatement.executeUpdate(
            "INSERT OR REPLACE INTO info (record, data) VALUES (" +
                "'pinit-version', '" +
                plugin.getDescription().getVersion() + "')"
            );
            updateDbVersionStatement.close();
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }
    private void pushNewConfig(){
        plugin.getLogger().info("Pushing new config file...");
        plugin.saveResource("config.yml", true);
        plugin.reloadConfig();
        plugin.config = plugin.getConfig();
    }
}
