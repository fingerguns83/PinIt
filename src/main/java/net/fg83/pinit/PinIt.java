package net.fg83.pinit;

import net.fg83.pinit.commands.*;
import net.fg83.pinit.completers.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;


public final class PinIt extends JavaPlugin implements Listener {

    PinIt plugin = this;
    public Connection connection;
    public FileConfiguration config;

    public boolean mvEnabled = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        File databaseFile = new File(getDataFolder(), "pins.db");
        String databasePath = databaseFile.getAbsolutePath();

        try {
            Class.forName("org.sqlite.JDBC");

            databaseFile.createNewFile();
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

            createServerTable();
            createPlayersTable();
            createInfoTable();
        }
        catch (ClassNotFoundException | SQLException | IOException e){
            getLogger().info("Database error: {" + e.getMessage() + "}");
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.versionCheck();
            }
        }, 300);

        plugin.getServer().getPluginManager().registerEvents(this, this);

        plugin.getCommand("pinit").setExecutor(new PinItCommand(this));
        plugin.getCommand("pinit").setTabCompleter(new PinItCompleter());

        plugin.getCommand("makepin").setExecutor(new MakePinCommand(this));
        plugin.getCommand("makepin").setTabCompleter(new MakePinCompleter(this));

        plugin.getCommand("pinlist").setExecutor(new PinListCommand(this));
        plugin.getCommand("pinlist").setTabCompleter(new PinListCompleter(this));

        plugin.getCommand("deletepin").setExecutor(new DeletePinCommand(this));
        plugin.getCommand("deletepin").setTabCompleter(new DeletePinCompleter());

        plugin.getCommand("serverpinit").setExecutor(new ServerPinItCommand(this));
        plugin.getCommand("serverpinit").setTabCompleter(new PinItCompleter());

        plugin.getCommand("makeserverpin").setExecutor(new ServerMakePinCommand(this));
        plugin.getCommand("makeserverpin").setTabCompleter(new MakePinCompleter(this));

        plugin.getCommand("deleteserverpin").setExecutor(new DeleteServerPinCommand(this));
        plugin.getCommand("deleteserverpin").setTabCompleter(new DeletePinCompleter());

        plugin.getCommand("sharepin").setExecutor(new SharePinCommand(this));
        plugin.getCommand("sharepin").setTabCompleter(new SharePinCompleter(this));

        //plugin.getCommand("findpin").setExecutor(new FindPinCommand(this));
        //plugin.getCommand("findpin").setTabCompleter(new FindPinCompleter(this));

    }

    @Override
    public void onDisable() {
        // Close the database connection when the plugin is disabled
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                getLogger().info("Database error: {" + e.getMessage() + "}");
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        createPlayerTable(player);
        updatePlayersTable(player);
    }

    // TABLE INITIATION
    private void createServerTable() {
        String statement = "CREATE TABLE IF NOT EXISTS server " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "location_world TEXT NOT NULL, " +
                "locationX INTEGER NOT NULL, " +
                "locationY INTEGER NOT NULL, " +
                "locationZ INTEGER NOT NULL, " +
                "category TEXT DEFAULT 'uncategorized')";
        sendSQLCommand(statement);
    }
    private void createPlayersTable() {
        String statement = "CREATE TABLE IF NOT EXISTS players " +
                "(uuid TEXT NOT NULL UNIQUE, " +
                "name TEXT NOT NULL)";
        sendSQLCommand(statement);
    }
    private void createInfoTable() {
        String input = "CREATE TABLE IF NOT EXISTS info (record TEXT NOT NULL, data TEXT NOT NULL)";
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(input);
                statement.close();
                versionCheck();
            }
            catch (SQLException e){
                getLogger().info("Database error: {" + e.getMessage() + "}");
            }
        });
    }
    public void createPlayerTable(Player player){
        String statement = "CREATE TABLE IF NOT EXISTS player" + player.getUniqueId().toString().replace("-", "") + " " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "location_world TEXT NOT NULL, " +
                "locationX INTEGER NOT NULL, " +
                "locationY INTEGER NOT NULL, " +
                "locationZ INTEGER NOT NULL, " +
                "category TEXT DEFAULT 'uncategorized')";
        sendSQLCommand(statement);
    }
    public void updatePlayersTable(Player player){
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("INSERT OR REPLACE INTO players (uuid, name) VALUES ('" +
                    player.getUniqueId().toString().replace("-", "") + "', '" +
                    player.getName() + "')"
            );
        }
        catch (SQLException e){
            getLogger().info("Database error: {" + e.getMessage() + "}");
        }
    }

    // SQL Calls
    public void sendSQLCommand(String input){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(input);
            }
            catch (SQLException e){
                getLogger().info("Database error: {" + e.getMessage() + "}");
            }
        });
    }

    // Communication Functions
    public void sendPinItMessage(Player player, String message, boolean error){
        TextComponent output = new TextComponent("PinIt >> ");
        output.setColor(ChatColor.AQUA);
        output.setBold(true);
        if (error){
            TextComponent errorMessage = new TextComponent(message);
            errorMessage.setColor(ChatColor.RED);
            errorMessage.setBold(false);
            output.addExtra(errorMessage);
        }
        else {
            TextComponent notif = new TextComponent(message);
            notif.setColor(ChatColor.WHITE);
            notif.setBold(false);
            output.addExtra(notif);
        }
        player.spigot().sendMessage(output);
    }

    // MISC
    public String getNamespacedWorld(String world, boolean ignoreNamespace){
        String vanillaOverworld = config.getString("vanilla-overworld");
        String vanillaNether = config.getString("vanilla-nether");
        String vanillaEnd = config.getString("vanilla-end");
        if (vanillaOverworld.equalsIgnoreCase(world)){
            if (ignoreNamespace){
                return "Overworld";
            }
            return "minecraft:overworld";
        }
        else if (vanillaNether.equalsIgnoreCase(world)){
            if (ignoreNamespace){
                return "Nether";
            }
            return "minecraft:the_nether";
        }
        else if (vanillaEnd.equalsIgnoreCase(world)){
            if (ignoreNamespace){
                return "End";
            }
            return "minecraft:the_end";
        }
        else {
            return world;
        }
    }

    public void versionCheck() {
        try {
            String versionCheckQuery = "SELECT * FROM info WHERE record='pinit-version'";
            Statement versionCheckStatement = connection.createStatement();
            ResultSet versionCheck = versionCheckStatement.executeQuery(versionCheckQuery);
            if (versionCheck.next()) {
                versionCheck.getString("data");
            } else {
                String input = "INSERT INTO info (record, data) VALUES ('pinit-version', '" + plugin.getDescription().getVersion() + "')";
                Statement statement = connection.createStatement();
                statement.executeUpdate(input);
            }
        }
        catch (SQLException e){
            getLogger().info(e.getMessage());
        }
    }
}
