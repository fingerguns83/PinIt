package net.fg83.pinit;

import net.fg83.pinit.commands.*;
import net.fg83.pinit.completers.*;
import net.fg83.pinit.tasks.*;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;


public final class PinIt extends JavaPlugin implements Listener {
    final PinIt plugin = this;
    LuckPerms luckPermsApi;
    public final String versionString = plugin.getDescription().getVersion();

    public Connection connection;
    public FileConfiguration config;
    public TagList serverTags;
    public final Map<Player, TagList> playerTagLists = new HashMap<>();
    public final Map<String, PinItWorld> worldById = new HashMap<>();
    public final Map<String, PinItWorld> worldByName = new HashMap<>();


    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        try {
            makeDatabaseConnection();
            createInfoTable();

            // Check if database needs updated.
            if (versionCheck()){
                getLogger().info("Your PinIt database is out of date. Starting updater...");
                new Updater(plugin).update();
            }
            else {
                setDbVersion();
            }

            // Create required tables if they don't exist
            createServerTable();
            createPlayersTable();
            createWorldsTable();
            updateWorldsTable();

            // Create optional tables if configured.
            if (config.getBoolean("enable-death-pins")){
                createDeathPinsTable();
            }

            if (config.getBoolean("velocity")){
                createWarpsTable();
            }
        }
        catch (ClassNotFoundException | SQLException | IOException | RuntimeException e){
            getLogger().info(e.getMessage());
            plugin.setEnabled(false);
        }

        // Initiate Luck Perms
        if (Bukkit.getServicesManager().getRegistration(LuckPerms.class) != null){
            getLogger().info("FOUND LUCKPERMS");
            luckPermsApi = LuckPermsProvider.get();
            createPermissionsSection();
        }

        // Initialize Server Tag List
        serverTags = new TagList(null, this);

        // Refresh Tag Lists
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new TagListUpdater(this), 1200, 1200);

        // Refresh Server Maps
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new WorldMappingUpdater(this), 60, 6000);

        // Setup bStats
        int pluginId = 20056;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SimplePie("Implementation", () -> (config.getBoolean("velocity")) ? "Velocity" : "Standalone"));
        metrics.addCustomChart(new SimplePie("Database", () -> (config.getBoolean("mysql-enabled")) ? "MySQL" : "SQLite"));

        // Register Events
        plugin.getServer().getPluginManager().registerEvents(this, this);

        // Register Commands
        plugin.getCommand("pinit").setExecutor(new PinItCommand(this));
        plugin.getCommand("pinit").setTabCompleter(new PinItCompleter(this));

        plugin.getCommand("serverpinit").setExecutor(new PinItCommand(this));
        plugin.getCommand("serverpinit").setTabCompleter(new PinItCompleter(this));

        plugin.getCommand("makepin").setExecutor(new MakePinCommand(this));
        plugin.getCommand("makepin").setTabCompleter(new MakePinCompleter(this));

        plugin.getCommand("makeserverpin").setExecutor(new MakePinCommand(this));
        plugin.getCommand("makeserverpin").setTabCompleter(new MakePinCompleter(this));

        plugin.getCommand("deletepin").setExecutor(new DeletePinCommand(this));
        plugin.getCommand("deletepin").setTabCompleter(new NullCompleter());

        plugin.getCommand("deleteserverpin").setExecutor(new DeletePinCommand(this));
        plugin.getCommand("deleteserverpin").setTabCompleter(new NullCompleter());

        plugin.getCommand("sharepin").setExecutor(new SharePinCommand(this));

        plugin.getCommand("pinlist").setExecutor(new PinListCommand(this));
        plugin.getCommand("pinlist").setTabCompleter(new PinListCompleter(this));

        plugin.getCommand("deathpin").setExecutor(new DeathPinCommand(this));
        plugin.getCommand("deathpin").setTabCompleter(new DeathPinCompleter());

        plugin.getCommand("pinwarp").setExecutor(new PinWarpCommand(this));
        plugin.getCommand("pinwarp").setTabCompleter(new NullCompleter());

        plugin.getCommand("findpin").setExecutor(new FindPinCommand(this));
        plugin.getCommand("findpin").setTabCompleter(new NullCompleter());
    }

    @Override
    public void onDisable() {
        // Close the database connection when the plugin is disabled
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                getLogger().info(e.getMessage());
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        createPlayerTable(player);
        updatePlayersTable(player);
        updatePlayerTags(player);

        if (config.getBoolean("velocity")){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new CheckWarpTask(plugin, player));
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        // Get the player who died from the event
        Player player = event.getEntity();

        // Create a new Pin object representing the death location
        Pin deathPin = new Pin("Death Pin", "deathpin", player.getLocation(), plugin, player, true);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new MakePinTask(plugin, player, deathPin));
    }

    private void makeDatabaseConnection() throws ClassNotFoundException, SQLException, IOException{
        // Log a message indicating that a database connection is being established
        printDebug("Making database connection");

        // Check if MySQL is enabled in the configuration
        if (config.getBoolean("mysql-enable")){
            // Check if all required MySQL configuration parameters are set
            if ((config.isSet("mysql-ip") && config.isSet("mysql-port") && config.isSet("mysql-database") && config.isSet("mysql-user") && config.isSet("mysql-password")) || config.getBoolean("velocity-mode")) {
                // Extract MySQL configuration parameters from the configuration file
                String dbIp = config.getString("mysql-ip");
                String dbPort = config.getString("mysql-port");
                String dbName = config.getString("mysql-database");
                String dbUser = config.getString("mysql-user");
                String dbPass = config.getString("mysql-password");

                // Construct the JDBC URL for MySQL
                String jdbcURL = "jdbc:mysql://" + dbIp + ":" + dbPort + "/" + dbName;

                // Load the MySQL JDBC driver class
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establish a connection to the MySQL database
                this.connection = DriverManager.getConnection(jdbcURL, dbUser, dbPass);

                // Check if the connection is null, indicating a failure to connect
                if (connection == null) {
                    throw new SQLException("Failed to connect to MySQL");
                }
            }
            else {
                // Throw a runtime exception if the MySQL configuration is incomplete
                throw new RuntimeException("Invalid MySQL config");
            }
        }
        else {
            // If MySQL is not enabled, set up a connection to an SQLite database
            File databaseFile = new File(getDataFolder(), "pins.db");
            String databasePath = databaseFile.getAbsolutePath();

            // Load the SQLite JDBC driver class
            Class.forName("org.sqlite.JDBC");

            // Create the SQLite database file if it does not exist
            databaseFile.createNewFile();

            // Establish a connection to the SQLite database
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        }
    }

    // Plugin Setup
    public void addFancyNamesComments(){
        List<String> fancyNamesComments = new ArrayList<>();
        fancyNamesComments.add(null);
        fancyNamesComments.add("Fancy Names");
        fancyNamesComments.add("Set nicer names for the worlds on this server, rather than the names of their world directories");
        fancyNamesComments.add("(Note 1: Shorter is sweeter. Keep names under 16 characters.)");
        fancyNamesComments.add("(Note 2: Levels will populate on first run. Don't worry about guessing.)");
        fancyNamesComments.add("(Note 3: You can use spaces (make sure to wrap in quotes), but don't use dashes.)");
        fancyNamesComments.add("(Note 4: If using Velocity, be sure to set unique names for every world. I'm saving you from yourself here.)");
        fancyNamesComments.add("e.g.");
        fancyNamesComments.add("world: Industrial");
        fancyNamesComments.add("world_nether: Mining Nether");
        config.setComments("fancy-names", fancyNamesComments);
    }
    public void createPermissionsSection(){
        // Initialize LuckPerms integration config
        ConfigurationSection luckPermsSection;

        // If the "luckperms" section does not exist in the configuration, create it and set default comments
        if (!config.contains("luckperms")){
            luckPermsSection = config.createSection("luckperms");

            // Set comments for LuckPerms integration
            List<String> lpComments = new ArrayList<>();
            lpComments.add(null);
            lpComments.add("LuckPerms integration");
            lpComments.add("Enable \"lp-pin-limits\" to vary the personal pin limit by permission group");
            lpComments.add("Player will get the highest limit of any group they belong to.");
            lpComments.add("This will override \"personal-pin-limit\" above.");
            lpComments.add("LuckPerms groups will auto-populate if the plugin is enabled.");
            lpComments.add("(Set to -1 for unlimited.)");
            config.setComments("luckperms", lpComments);
        }
        else {
            // If the "luckperms" section exists, retrieve it from the configuration
            luckPermsSection = config.getConfigurationSection("luckperms");
        }

        // Set default value for "lp-pin-limits" if not present
        if (!luckPermsSection.contains("lp-pin-limits")){
            config.set("luckperms.lp-pin-limits", false);
        }

        // If LuckPerms API is not available, save the configuration and return
        if (luckPermsApi == null){
            saveConfig();
            return;
        }

        // Check and configure pin limit groups for LuckPerms
        if (!config.contains("luckperms.pin-limit-groups")){
            // If "luckperms.pin-limit-groups" does not exist, create it and set default comments
            printDebug("\"luckperms.pin-limit-groups\" not found.");
            config.createSection("luckperms.pin-limit-groups");
            saveConfig();

            // Set comments for the pin limit groups configuration
            List<String> groupComments = new ArrayList<>();
            groupComments.add(null);
            config.setComments("luckperms.pin-limit-groups", groupComments);
            saveConfig();
        }

        // Retrieve loaded LuckPerms groups
        List<Group> lpGroups = luckPermsApi.getGroupManager().getLoadedGroups().stream().toList();

        // Iterate through LuckPerms groups and initialize pin limits for each group (if group does not exist in config)
        for (Group group : lpGroups){
            printDebug("Checking group config \"" + group.getName() + "\"");
            if (!config.isSet("luckperms.pin-limit-groups." + group.getName())){
                printDebug("Group config not set.");
                config.set("luckperms.pin-limit-groups." + group.getName(), 0);
            }
        }
        saveConfig();
    }
    private void createServerTable() throws SQLException{
        printDebug("Creating global_pins table...");

        String inputString;

        if (config.getBoolean("mysql-enable")){
            inputString = "CREATE TABLE IF NOT EXISTS global_pins (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "name varchar(32) NOT NULL, " +
                "location_world varchar(64) NOT NULL, " +
                "locationX INT NOT NULL, " +
                "locationY INT NOT NULL, " +
                "locationZ INT NOT NULL, " +
                "category varchar(24))";
        }
        else {
            inputString = "CREATE TABLE IF NOT EXISTS global_pins (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL, " +
                "location_world TEXT NOT NULL, " +
                "locationX INTEGER NOT NULL, " +
                "locationY INTEGER NOT NULL, " +
                "locationZ INTEGER NOT NULL, " +
                "category TEXT)";
        }

        Statement createServerTableStatement = connection.createStatement();
        createServerTableStatement.executeUpdate(inputString);
        createServerTableStatement.close();
    }
    private void createPlayersTable() throws SQLException{
        printDebug("Creating players table...");

        String inputString;

        if (config.getBoolean("mysql-enable")){
            inputString = "CREATE TABLE IF NOT EXISTS players (" +
                "player_id VARCHAR(64) NOT NULL UNIQUE, " +
                "name TEXT NOT NULL)";
        }
        else {
            inputString = "CREATE TABLE IF NOT EXISTS players (" +
                "player_id TEXT NOT NULL UNIQUE, " +
                "name TEXT NOT NULL)";
        }

        Statement createPlayersTableStatement = connection.createStatement();
        createPlayersTableStatement.executeUpdate(inputString);
        createPlayersTableStatement.close();
    }
    private void createInfoTable() throws SQLException{
        printDebug("Creating info table...");

        String inputString;

        if (config.getBoolean("mysql-enable", false)){
            inputString = "CREATE TABLE IF NOT EXISTS info (record VARCHAR(255) NOT NULL UNIQUE, data TEXT NOT NULL)";
        }
        else {
            inputString = "CREATE TABLE IF NOT EXISTS info (record TEXT NOT NULL UNIQUE, data TEXT NOT NULL)";
        }

        Statement createInfoTableStatement = connection.createStatement();
        createInfoTableStatement.executeUpdate(inputString);
        createInfoTableStatement.close();
    }
    private void createWorldsTable() throws SQLException{
        printDebug("Creating worlds table...");

        String inputString;

        if (config.getBoolean("mysql-enable")){
            inputString = "CREATE TABLE IF NOT EXISTS worlds (" +
                "world_id VARCHAR(64) NOT NULL UNIQUE, " +
                "fancy_name TEXT NOT NULL, " +
                "server TEXT NOT NULL)";
        }
        else {
            inputString = "CREATE TABLE IF NOT EXISTS worlds (" +
                    "world_id TEXT NOT NULL UNIQUE, " +
                    "fancy_name TEXT NOT NULL, " +
                    "server TEXT NOT NULL)";
        }

        Statement createWorldsTableStatement = connection.createStatement();
        createWorldsTableStatement.executeUpdate(inputString);
        createWorldsTableStatement.close();
    }
    private void createWarpsTable() throws SQLException {
        printDebug("Creating warps table...");

        Statement createWarpsTableStatement = connection.createStatement();
        createWarpsTableStatement.executeUpdate("CREATE TABLE IF NOT EXISTS warps " +
                "(id INT PRIMARY KEY AUTO_INCREMENT, " +
                "player_id varchar(64) UNIQUE NOT NULL, " +
                "server TEXT NOT NULL, " +
                "location_world TEXT NOT NULL, " +
                "locationX INT NOT NULL, " +
                "locationY INT NOT NULL, " +
                "locationZ INT NOT NULL)"
        );
        createWarpsTableStatement.close();

        Statement deleteWarpStatement = connection.createStatement();
        deleteWarpStatement.executeUpdate("DELETE FROM warps");
        deleteWarpStatement.close();
    }
    private void createDeathPinsTable() throws SQLException{
        printDebug("Creating death_pins table...");

        String inputString;
        if (config.getBoolean("mysql-enable")){
            inputString = "CREATE TABLE IF NOT EXISTS death_pins (" +
                    "player_id varchar(64) NOT NULL UNIQUE, " +
                    "location_world varchar(64) NOT NULL, " +
                    "locationX INT NOT NULL, " +
                    "locationY INT NOT NULL, " +
                    "locationZ INT NOT NULL)";
        }
        else {
            inputString = "CREATE TABLE IF NOT EXISTS death_pins (" +
                    "player_id TEXT NOT NULL UNIQUE, " +
                    "location_world TEXT NOT NULL, " +
                    "locationX INTEGER NOT NULL, " +
                    "locationY INTEGER NOT NULL, " +
                    "locationZ INTEGER NOT NULL)";
        }

        Statement createDeathPinsTableStatement = connection.createStatement();
        createDeathPinsTableStatement.executeUpdate(inputString);
        createDeathPinsTableStatement.close();
    }

    // CREATE PERSONAL TABLES
    public void createPlayerTable(Player player){
        printDebug("Creating player table (" + player.getName() + ")...");

        String input;

        if (config.getBoolean("mysql-enable")){
            input = "CREATE TABLE IF NOT EXISTS player" + player.getUniqueId().toString().replace("-", "") + " (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name TEXT NOT NULL, " +
                "location_world TEXT NOT NULL, " +
                "locationX INT NOT NULL, " +
                "locationY INT NOT NULL, " +
                "locationZ INT NOT NULL, " +
                "category varchar(64) DEFAULT 'uncategorized')";
        }
        else {
            input = "CREATE TABLE IF NOT EXISTS player" + player.getUniqueId().toString().replace("-", "") + " " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "location_world TEXT NOT NULL, " +
                "locationX INTEGER NOT NULL, " +
                "locationY INTEGER NOT NULL, " +
                "locationZ INTEGER NOT NULL, " +
                "category TEXT DEFAULT 'uncategorized')";
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new DatabaseUpdateTask(plugin, input));
    }

    // Database Updates
    private void updateWorldsTable() throws SQLException{
        printDebug("Updating worlds table...");

        // Retrieve a list of all worlds on the server
        List<World> allWorlds = getServer().getWorlds();

        // Check and create the "fancy-names" section in the configuration if it does not exist
        if (!config.contains("fancy-names")){
            config.createSection("fancy-names");

            // Add comments for the "fancy-names" section
            addFancyNamesComments();
        }

        // Iterate through each world to update the "worlds" table
        for (World world : allWorlds){
            // Initialize variables for the fancy name and query input strings
            String fancyName;
            String inputString;

            // Check if a fancy name is set in the configuration for the current world
            if (!config.isSet("fancy-names." + world.getName())){
                // If not set, set the fancy name to the default (world name)
                config.set("fancy-names." + world.getName(), world.getName());
                fancyName = world.getName();
            }
            else if (Objects.requireNonNull(config.getString("fancy-names." + world.getName())).length() > 16) {
                // If the length of the fancy name exceeds 16 characters, truncate it
                fancyName = config.getString("fancy-names." + world.getName().substring(0, 15));
            }
            else {
                // Otherwise, use the configured fancy name
                fancyName = config.getString("fancy-names." + world.getName());
            }

            if (config.getBoolean("mysql-enable")){
                // For MySQL, construct the SQL statement for insertion or update
                inputString = "INSERT INTO worlds (world_id, fancy_name, server) VALUES ('" +
                    world.getUID() + "', '" +
                    fancyName + "', '" +
                    config.getString("server-name") + "') " +
                    "ON DUPLICATE KEY UPDATE " +
                    "fancy_name = '" + config.getString("fancy-names." + world.getName()) + "', " +
                    "server = '" + config.getString("server-name") + "'";
            }
            else {
                // For SQLite, construct the SQL statement for insertion or replacement
                inputString = "INSERT OR REPLACE INTO worlds (world_id, fancy_name, server) VALUES ('" +
                    world.getUID() + "', '" +
                    fancyName + "', '" +
                    config.getString("server-name") + "')";
            }

            // Create a statement and execute the SQL update for the current world
            Statement statement = connection.createStatement();
            statement.executeUpdate(inputString);
        }

        saveConfig();
        reloadConfig();
        config = getConfig();
    }
    public void updatePlayersTable(Player player){
        printDebug("Updating players table (" + player.getName() + ")...");

        String input;

        if (config.getBoolean("mysql-enable")){
            input = "INSERT INTO players (player_id, name) VALUES ('" +
                player.getUniqueId().toString().replace("-", "") + "', '" +
                player.getName() + "') " +
                "ON DUPLICATE KEY UPDATE name = '" + player.getName() + "'";
        }
        else {
            input = "INSERT OR REPLACE INTO players (player_id, name) VALUES ('" +
                player.getUniqueId().toString().replace("-", "") + "', '" +
                player.getName() + "')";
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new DatabaseUpdateTask(plugin, input));
    }
    public void setDbVersion(){
        String input;

        if (config.getBoolean("mysql-enable")){
            input = "INSERT INTO info (record, data) VALUES (" +
                    "'pinit-version', '" +
                    plugin.getDescription().getVersion() + "') " +
                    "ON DUPLICATE KEY UPDATE data='" + plugin.getDescription().getVersion() + "'";
        }
        else {
            input = "INSERT OR REPLACE INTO info (record, data) VALUES (" +
                    "'pinit-version', '" +
                    plugin.getDescription().getVersion() + "')";
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new DatabaseUpdateTask(plugin, input));
    }

    // Fetches
    public void updatePlayerTags(Player player){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new PlayerTagUpdater(plugin, player));
    }
    public List<String> getWorldNames(Boolean fancy){
        List<String> output = new ArrayList<>();
        if (fancy){
            worldById.forEach((key, value) -> output.add(value.getFancyName()));
        }
        else {
            worldById.forEach((key, value) -> output.add(value.getSafeName()));
        }

        return output;
    }
    public String getPinItWorldId(String worldName){
        return worldByName.get(worldName.replace("-", " ")).getWorldId();
    }
    public String getPinItWorldName(String worldId, Boolean fancy) {
        PinItWorld world = worldById.get(worldId);
        if (fancy){
            return world.getFancyName();
        }
        else {
            return world.getSafeName();
        }
    }
    public List<String> getAllTags(@Nullable Player player){
        if (player != null){
            if (playerTagLists.containsKey(player)){
                List<String> playerTags = playerTagLists.get(player).getTags();
                if (!playerTags.isEmpty()){
                    return playerTags;
                }
                else {
                    return new ArrayList<>();
                }
            }
            else {
                return new ArrayList<>();
            }
        }
        else {
            return serverTags.getTags();
        }
    }

    // Data Validation
    public Boolean validatePinName(String name, Player player){
        if (name.length() > 20){
            plugin.sendPinItMessage(player, "Pin name must be 24 characters or less.", true);
            return false;
        }
        else {
            return true;
        }
    }
    public Boolean validatePinTag(String tag, Player player){
        if (tag.contains(" ")){
            plugin.sendPinItMessage(player, "Pin category cannot contain spaces.", true);
            return false;
        }
        else if (tag.equalsIgnoreCase("#all")){
            plugin.sendPinItMessage(player, "That tag's reserved. Quit trying to break stuff. I knew your type would try this.", true);
            return false;
        }
        else if (tag.length() > 16){
            plugin.sendPinItMessage(player, "Pin category must be 16 characters or less.", true);
            return false;
        }
        else {
            return true;
        }
    }
    public Boolean validateWorldName(String safeWorldName){
        boolean output = false;
        for (String world : plugin.getWorldNames(false)){
            if (world.equalsIgnoreCase(safeWorldName)){
                output = true;
                break;
            }
        }
        return output;
    }

    // Pin Limits
    public boolean checkPinCount(Player player){
        int count = 0;
        try {
            // Create a statement to execute an SQL query and execute it
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS row_count FROM player" + player.getUniqueId().toString().replace("-", ""));

            // If the query is successful, retrieve the pin count
            if (resultSet.next()){
                count = resultSet.getInt("row_count");
            }

            // Get the maximum pin count allowed for the player
            int pinLimit = getMaxPinCount(player);

            // Check if the player has not reached their pin limit or has unlimited pins
            if (pinLimit == -1 || count < pinLimit){
                return true;
            }
            else {
                // Inform the player that they've reached their pin limit
                sendPinItMessage(player, "You've run out of pin slots (" + pinLimit + "). You'll need to make some space to add more.", true);
                return false;
            }

        }
        catch (SQLException e){
            // Log any SQLException and send an error message to the player
            getLogger().info(e.getMessage());
            sendPinItMessage(player, "Something went wrong. PinIt:644", true);
        }
        return false; // Return false in case of an exception or error
    }
    public Integer getMaxPinCount(Player player){
        // Fingy Bypass
        if (player.getUniqueId().toString().equalsIgnoreCase("8a311fee-2742-4f56-98d5-a6ab33a1cf28")){
            return -1;
        }

        // Check if LuckPerms pin limits are enabled and LuckPerms API is available
        if (!config.isSet("luckperms.lp-pin-limits") || !config.getBoolean("luckperms.lp-pin-limits") || (luckPermsApi == null)){
            // If LuckPerms integration is not active, or if it's not configured, use static personal pin limit configuration or default
            if (config.isSet("personal-pin-limit")){
                return config.getInt("personal-pin-limit");
            }
            else {
                return 12; // Default maximum pin count
            }
        }

        int personalLimit = 0;

        // Get the LuckPerms user for the player
        User luckPermsUser = luckPermsApi.getUserManager().getUser(player.getUniqueId());

        // If LuckPerms user is not found, use static personal pin limit configuration or default
        if (luckPermsUser == null){
            if (config.isSet("personal-pin-limit")){
                return config.getInt("personal-pin-limit");
            }
            else {
                return 12; // Default maximum pin count
            }
        }

        // Get the list of LuckPerms groups configured in the plugin's settings
        List<String> lpConfigGroups = config.getConfigurationSection("luckperms.pin-limit-groups").getKeys(true).stream().toList();

        // Iterate through LuckPerms groups to determine the maximum pin limit for the player
        for (String group : lpConfigGroups){
            // Remove the prefix to get the group name
            group = group.replace("luckperms.pin-limit-groups.", "");

            // Create a LuckPerms node representing membership in the group
            Node groupNode = Node.builder("group." + group).value(true).build();

            // Check if the player has the group node in their LuckPerms data
            if (luckPermsUser.data().contains(groupNode, NodeEqualityPredicate.EXACT).asBoolean()){
                // Retrieve the pin limit configured for the group
                int pinLimit = config.getInt("luckperms.pin-limit-groups." + group);

                // Update the personal limit if the group's limit is higher
                if (pinLimit > personalLimit) {
                    personalLimit = config.getInt("luckperms.pin-limit-groups." + group);

                }

                // If the group has unlimited pins, return -1 as the maximum pin count
                if (pinLimit == -1){
                    return -1;
                }
            }
        }
        return personalLimit; // Return the determined personal pin limit
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
    public void sendPinItMessage(Player player, TextComponent message){
        TextComponent output = new TextComponent("PinIt >> ");
        output.setColor(ChatColor.AQUA);
        output.setBold(true);

        output.addExtra(message);

        player.spigot().sendMessage(output);
    }

    // Utilities
    public void printDebug(String message){
        if (config.contains("fingy-debug-mode") && config.getBoolean("fingy-debug-mode")){
            getLogger().info(message);
        }
    }
    public static List<String> parseArguments(String[] args) {
        // Initialize list to store parsed arguments
        List<String> parsedArgs = new ArrayList<>();

        // StringBuilder to construct the temporary argument
        StringBuilder tempArg = new StringBuilder();

        // Iterate through each argument in the array
        for (String arg : args){

            // Check if the temporary argument is not empty (Meaning that last argument was not terminated)
            if (tempArg.length() > 0){
                // If the argument starts with a double quote, it might be the end of the previous argument
                if (arg.startsWith("\"")){
                    // Add the current argument (remove double quotes) to the list
                    parsedArgs.add(tempArg.toString().replace("\"", "").trim());

                    // Reset the StringBuilder for the next argument
                    tempArg = new StringBuilder();
                }

                // Append the temporary argument with a space and the current argument
                tempArg.append(" ").append(arg);

                // If the argument ends with a double quote, it's the end of the temporary argument
                if (arg.endsWith("\"")){
                    // Add the temporary argument (remove double quotes) to the output list
                    parsedArgs.add(tempArg.toString().replace("\"", "").trim());

                    // Reset the StringBuilder for the next argument
                    tempArg = new StringBuilder();
                }
            }
            // If the current argument is empty and the argument starts with a double quote, it's the start of a new argument
            else if (arg.startsWith("\"")){
                // Append the argument to the temporary argument
                tempArg.append(arg.trim());

                // If the argument ends with a double quote, it's the end of the current argument
                if (arg.endsWith("\"")){
                    // Add the temporary argument (remove double quotes) to the output list
                    parsedArgs.add(tempArg.toString().replace("\"", "").trim());

                    // Reset the StringBuilder for the next argument
                    tempArg = new StringBuilder();
                }
            }
            // If the current argument is empty and the argument does not start with a double quote, it's a standalone argument
            else {
                // If the argument ends with a double quote, remove the double quotes
                if (arg.endsWith("\"")){
                    arg = arg.replace("\"", "");
                }

                // Append the argument to the temporary argument
                tempArg.append(arg);

                // Add the temporary argument to the list
                parsedArgs.add(tempArg.toString());

                // Reset the StringBuilder for the next argument
                tempArg = new StringBuilder();
            }
        }
        return parsedArgs; // Return the list of parsed arguments
    }

    // VERSION CHECK. YOU'LL WANT TO ADDRESS THIS FOR FUTURE UPDATES.
    public boolean versionCheck() {
        // Check if MySQL is enabled; if so, skip version checking.
        if (getConfig().getBoolean("mysql-enabled", false)){
            return false;
        }

        try {
            // Define the SQL query to retrieve the stored plugin version from the database.
            String versionCheckQuery = "SELECT * FROM info WHERE record='pinit-version'";
            Statement versionCheckStatement = connection.createStatement();
            ResultSet versionCheck = versionCheckStatement.executeQuery(versionCheckQuery);

            // Check if a record with the plugin version exists in the database.
            if (versionCheck.next()) {
                // Retrieve the stored database version.
                String dbVersion = versionCheck.getString("data").trim();

                // Close the statement.
                versionCheckStatement.close();

                // Check if the database version matches the current plugin version.
                if (dbVersion.trim().equalsIgnoreCase(plugin.getDescription().getVersion())){
                    return false;
                }
                else {
                    // Check if the database version is 1.0, indicating a major update is required.
                    if (dbVersion.equals("1.0")) {
                        return true;
                    }
                    else {
                        // Otherwise, versions do not match, and no major update is required.
                        return false;
                    }
                }
            }
            else {
                // Close the statement since the record does not exist in the database.
                versionCheckStatement.close();

                // No major update is required as this is the first entry in the database.
                return false;
            }
        }
        catch (SQLException e){
            // Log any SQL exceptions that may occur.
            getLogger().info(e.getMessage());
            throw new RuntimeException("Something went wrong with version checking!");
        }
    }

}
