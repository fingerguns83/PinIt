package net.fg83.pinit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Pin {
    final PinIt plugin;
    private Integer pinId;
    private String pinName;
    private String category;
    private String server;
    private String worldId;
    private String worldName;
    private String worldFancyName;
    private Integer locationX;
    private Integer locationY;
    private Integer locationZ;
    private Boolean global = false;
    private Boolean deathPin = false;
    private Player owner;

    // Make Pin From Location
    public Pin(String name, String category, Location location, PinIt plugin, @Nullable Player owner, Boolean isDeath){
        assert location.getWorld() != null;

        this.plugin = plugin;

        if (owner == null){
            this.global = true;
        }
        else {
            this.owner = owner;
        }

        this.pinName = name;
        this.category = category;

        this.server = plugin.config.getString("server-name");
        this.worldId = location.getWorld().getUID().toString();
        this.worldName = plugin.getPinItWorldName(worldId, false);
        this.worldFancyName = plugin.getPinItWorldName(worldId, true);

        this.locationX = location.getBlockX();
        this.locationY = location.getBlockY();
        this.locationZ = location.getBlockZ();

        this.deathPin = isDeath;
    }
    // Make Pin Manually
    public Pin(String name, String category, String worldName, Integer locationX, Integer locationY, Integer locationZ, PinIt plugin, @Nullable Player owner, Boolean isDeath){
        this.plugin = plugin;

        if (owner == null){
            this.global = true;
        }
        else {
            this.owner = owner;
        }

        this.pinName = name;
        this.category = category;

        this.worldName = worldName;
        this.worldFancyName = worldName.replace("-", " ");
        this.worldId = plugin.getPinItWorldId(this.worldName);

        this.server = this.plugin.worldById.get(this.worldId).getServer();

        this.locationX = locationX;
        this.locationY = locationY;
        this.locationZ = locationZ;

        this.deathPin = isDeath;
    }

    // Make Pin From Database
    public Pin(ResultSet entry, @Nullable Player owner, PinIt plugin, Boolean isDeath){
        this.plugin = plugin;

        if (isDeath){
            this.deathPin = true;
            plugin.printDebug("Building Death Pin");
            this.pinName = "Death Pin";
            plugin.printDebug("Pin Name: " + pinName);
            this.category = "deathpin";
            plugin.printDebug("Category: " + category);
            this.owner = owner;

            try {
                this.worldId = entry.getString("location_world");
                plugin.printDebug("worldId: " + worldId);
                this.locationX = entry.getInt("locationX");
                plugin.printDebug("locationX: " + locationX);
                this.locationY = entry.getInt("locationY");
                plugin.printDebug("locationY: " + locationY);
                this.locationZ = entry.getInt("locationZ");
                plugin.printDebug("locationZ: " + locationZ);
            }
            catch (SQLException e){
                plugin.printDebug("SQL Exception while building Death Pin");
                plugin.getLogger().info(e.getMessage());
                return;
            }
        }
        else {
            if (owner == null){
                this.global = true;
            }
            else {
                this.owner = owner;
            }

            try {
                this.pinId = entry.getInt("id");
                this.pinName = entry.getString("name");
                this.worldId = entry.getString("location_world");
                this.locationX = entry.getInt("locationX");
                this.locationY = entry.getInt("locationY");
                this.locationZ = entry.getInt("locationZ");
                this.category = entry.getString("category");
            }
            catch (SQLException e){
                plugin.getLogger().info(e.getMessage());
                return;
            }
        }

        this.server = this.plugin.worldById.get(this.worldId).getServer();
        this.worldName = plugin.getPinItWorldName(worldId, false);
        this.worldFancyName = plugin.getPinItWorldName(worldId, true);
    }

    public void setPinId(int pinId){this.pinId = pinId;}
    public String getName() {
        return this.pinName;
    }
    public String getWorldId(){
        return this.worldId;
    }
    public Integer getLocationX() {
        return locationX;
    }
    public Integer getLocationY() {
        return locationY;
    }
    public Integer getLocationZ() {
        return locationZ;
    }
    public String getServer(){
        return this.server;
    }
    public Boolean getGlobal() {
        return global;
    }
    public Boolean getDeathPin(){
        return deathPin;
    }

    public String getCoordinatesString(Boolean command){
        String separator;
        if (!command){
            separator = ", ";
        }
        else {
            separator = " ";
        }

        return this.locationX + separator + this.locationY + separator + this.locationZ;
    }
    public Boolean store(){
        plugin.printDebug("Storing pin...");

        String input;

        // Check if it's a Death Pin
        if (deathPin){
            // Construct the SQL query to insert or update a Death Pin entry
            if (plugin.config.getBoolean("mysql-enable")){
                input = "INSERT INTO death_pins (player_id, location_world, locationX, locationY, locationZ) VALUES ('" +
                        owner.getUniqueId() + "', '" +
                        worldId + "', " +
                        locationX + ", " +
                        locationY + ", " +
                        locationZ + ") " +
                        "ON DUPLICATE KEY UPDATE " +
                        "location_world = '" + worldId + "', " +
                        "locationX = " + locationX + ", " +
                        "locationY = " + locationY + ", " +
                        "locationZ = " + locationZ;
            }
            else {
                input = "INSERT OR REPLACE INTO death_pins (player_id, location_world, locationX, locationY, locationZ) VALUES ('" +
                        owner.getUniqueId() + "', '" +
                        worldId + "', " +
                        locationX + ", " +
                        locationY + ", " +
                        locationZ + ")";
            }

            try {
                // Execute the SQL query to insert or update the Death Pin entry
                Statement makeDeathPinStatement = plugin.connection.createStatement();
                makeDeathPinStatement.executeUpdate(input);
                makeDeathPinStatement.close();
            }
            catch (SQLException e){
                plugin.getLogger().info(e.getMessage());
                return false;
            }

            return true;
        }

        // Check if it's a global pin
        if (global){
            input = "INSERT INTO global_pins (name, location_world, locationX, locationY, locationZ, category) VALUES (?, ?, ?, ?, ?, ?)";
        }
        else {
            // If not global, create a table for the player if it doesn't exist
            plugin.createPlayerTable(owner);

            // Check if the player has reached their pin limit
            if (!plugin.checkPinCount(owner)){
                return false;
            }
            // Construct the SQL query to insert a player-specific pin entry
            input = "INSERT INTO player" + owner.getUniqueId().toString().replace("-", "") +
                    " (name, location_world, locationX, locationY, locationZ, category) VALUES (?, ?, ?, ?, ?, ?)";
        }

        try {
            // Prepare the SQL statement with placeholders
            PreparedStatement makeGlobalPinStatement = plugin.connection.prepareStatement(input);
            makeGlobalPinStatement.setString(1, this.pinName);
            makeGlobalPinStatement.setString(2, this.worldId);
            makeGlobalPinStatement.setInt(3, this.locationX);
            makeGlobalPinStatement.setInt(4, this.locationY);
            makeGlobalPinStatement.setInt(5, this.locationZ);
            makeGlobalPinStatement.setString(6, this.category);
            // Execute the prepared statement to insert the pin entry and close it.
            makeGlobalPinStatement.executeUpdate();
            makeGlobalPinStatement.close();
            return true;
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
            return false;
        }
    }
    public TextComponent makeTitle(){
        TextComponent title = new TextComponent(this.pinName);
        if (this.global){
            title.setColor(ChatColor.GOLD);
        }
        else {
            title.setColor(ChatColor.DARK_AQUA);
        }

        title.setItalic(true);

        TextComponent nameHoverText = new TextComponent(this.category);
        nameHoverText.setItalic(true);
        BaseComponent[] nameHoverComponents = new BaseComponent[]{nameHoverText};
        title.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, nameHoverComponents));

        return title;
    }
    public TextComponent makeShareButton(){
        TextComponent shareMessage;

        // Check if the pin is global or a death pin
        if (global || deathPin){
            shareMessage = new TextComponent("[+]");
            shareMessage.setColor(ChatColor.LIGHT_PURPLE);
            // If pin is global or a death pin, construct the command for adding the pin to the player's pin list
            String addPinCommand = "/makepin " + plugin.getPinItWorldName(worldId, false) +
                    " " + getCoordinatesString(true) + " ";

            // Set a ClickEvent to suggest the addPinCommand when the share message is clicked
            shareMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, addPinCommand));

            // Create a TextComponent for the hover text with instructions to add the pin to the pin list
            TextComponent hoverText = new TextComponent("Add this pin to your pin list.");
            hoverText.setItalic(true);
            BaseComponent[] hoverComponents = new BaseComponent[]{hoverText};

            // Set a HoverEvent to show the hoverText when the share message is hovered over
            shareMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));

        }
        else {
            shareMessage = new TextComponent("[→]");
            shareMessage.setColor(ChatColor.LIGHT_PURPLE);
            // For non-global and non-death pins, construct the command for sharing the pin with another player
            shareMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sharepin " + pinId + " "));

            // Create a TextComponent for the hover text with instructions to add the pin to the pin list
            TextComponent hoverText = new TextComponent("Send this pin to another player.");
            hoverText.setItalic(true);
            BaseComponent[] hoverComponents = new BaseComponent[]{hoverText};

            // Set a HoverEvent to show the hoverText when the share message is hovered over
            shareMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));
        }

        return shareMessage;
    }
    public TextComponent makeDeleteButton(Player player){
        TextComponent deleteMessage = new TextComponent("[x]");
        deleteMessage.setColor(ChatColor.DARK_RED);

        TextComponent hoverText = new TextComponent("Delete this pin.");
        hoverText.setItalic(true);
        BaseComponent[] hoverComponents = new BaseComponent[]{hoverText};
        deleteMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));

        if (this.global){
            if (player.hasPermission("pinit.server.delete")){
                deleteMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/deleteserverpin " + pinId));
            }
            else {
                return null;
            }
        }
        else {
            deleteMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/deletepin " + pinId));
        }
        deleteMessage.addExtra(" ");

        return deleteMessage;
    }
    public void sendShareMessage(Player target){
        // Create a TextComponent to construct the output message
        TextComponent output = new TextComponent();

        // Create a TextComponent for the accept message (the [+] button) with dark green color
        TextComponent acceptMessage = new TextComponent("[+] ");
        acceptMessage.setColor(ChatColor.LIGHT_PURPLE);

        // Construct the command for adding the pin to the target player's pin list
        String makePinCommand = "/makepin " + this.worldName +
                " " + getCoordinatesString(true) + " ";

        // Set a ClickEvent to suggest the /makepin command when the accept message is clicked
        acceptMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, makePinCommand));

        // Create a TextComponent for the hover text with instructions to add the pin to the pin list
        TextComponent hoverText = new TextComponent("Add this pin to your pin list.");
        hoverText.setItalic(true);
        BaseComponent[] hoverComponents = new BaseComponent[]{hoverText};

        // Set a HoverEvent to show the hoverText when the accept message is hovered over
        acceptMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));

        // Add the "accept message" to the output TextComponent
        output.addExtra(acceptMessage);

        // Create a TextComponent for the pin name with gold color and italic style
        TextComponent nameMessage = new TextComponent(this.pinName);
        nameMessage.setColor(ChatColor.GOLD);
        nameMessage.setItalic(true);
        output.addExtra(nameMessage);

        // Add a colon and space to separate the pin name and location information
        output.addExtra(": ");

        // Create a TextComponent for the location message with green color
        TextComponent locationMessage = new TextComponent("(" + this.worldFancyName + ") ");
        locationMessage.addExtra(this.getCoordinatesString(false));
        locationMessage.setColor(ChatColor.GREEN);

        // Add the location message to the output TextComponent
        output.addExtra(locationMessage);
        output.addExtra(" ");

        // Send a PinIt message to the target player indicating that the owner has shared a pin
        plugin.sendPinItMessage(target, owner.getName() + " has shared a pin with you!", false);

        // Send the constructed output TextComponent to the target player
        target.spigot().sendMessage(output);
    }
    public void sendMessage(Player player){
        // Create a TextComponent to construct the output message
        TextComponent output = new TextComponent();

        // Check if the pin is not global or the player has the "pinit.server.delete" permission
        if (!this.global || player.hasPermission("pinit.server.delete")){
            // If player can delete the pin, create the delete button.
            output.addExtra(makeDeleteButton(player));
        }

        // Add the share button to the output
        output.addExtra(makeShareButton());

        // Add a space to separate the buttons from the title
        output.addExtra(" ");

        // Create a TextComponent for the title
        TextComponent nameMessage = makeTitle();

        // Add the title to the output
        output.addExtra(nameMessage);

        // Add a colon and space to separate the title and location information
        output.addExtra(": ");

        // Create a TextComponent for the location message with green color
        TextComponent locationMessage = new TextComponent("(" + this.worldFancyName + ") ");
        locationMessage.addExtra(this.getCoordinatesString(false));
        locationMessage.setColor(ChatColor.GREEN);

        // Check if the player has the "pinit.warp" permission
        if (player.hasPermission("pinit.warp")){
            String warpCommand;

            // Construct the command for pinwarp based on whether the pin is global or not
            if (global){
                warpCommand = "/pinwarp server " + this.pinId;
            }
            else {
                warpCommand = "/pinwarp me " + this.pinId;
            }

            // Create a TextComponent for the hover text with instructions to click to warp
            TextComponent hoverText = new TextComponent("Click to warp.");

            // Create hover text for location message to indicate warping ability.
            hoverText.setItalic(true);
            BaseComponent[] hoverComponents = new BaseComponent[]{hoverText};

            // Set a HoverEvent to show the hoverText when the location message is hovered over
            locationMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));

            // Set a ClickEvent to run the warpCommand when the location message is clicked
            locationMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, warpCommand));
        }

        // Add the location message to the output TextComponent
        output.addExtra(locationMessage);

        // Send the constructed output TextComponent to the player
        player.spigot().sendMessage(output);
    }

    public void sendDeathMessage(Player player){
        // Check if it's not a death pin, and return if so
        if (!this.deathPin) {
            return;
        }

        // Create a TextComponent to construct the output message
        TextComponent output = new TextComponent();

        // Create a TextComponent for the accept message (the [+] button) with dark green color
        TextComponent acceptMessage = new TextComponent("[+]");
        acceptMessage.setColor(ChatColor.LIGHT_PURPLE);

        // Construct the command for adding the pin to the player's pin list
        String addPinCommand = "/makepin " + this.worldName +
                " " + getCoordinatesString(true) + " ";

        // Set a ClickEvent to suggest the addPinCommand when the accept message is clicked
        acceptMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, addPinCommand));

        // Create a TextComponent for the hover text with instructions to add the pin to the pin list
        TextComponent hoverText = new TextComponent("Add this pin to your pin list.");
        hoverText.setItalic(true);
        BaseComponent[] hoverComponents = new BaseComponent[]{hoverText};

        // Set a HoverEvent to show the hoverText when the accept message is hovered over
        acceptMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));

        // Add the "accept message" to the output TextComponent
        output.addExtra(acceptMessage);

        // Create a TextComponent for the pin name with gold color and italic style
        TextComponent nameMessage = new TextComponent(" *" + this.pinName);
        nameMessage.setColor(ChatColor.GOLD);
        nameMessage.setItalic(true);
        output.addExtra(nameMessage);

        // Add a colon and space to separate the pin name and location information
        output.addExtra(": ");

        // Create a TextComponent for the location message with green color
        TextComponent locationMessage = new TextComponent("(" + this.worldFancyName + ") ");
        locationMessage.addExtra(this.getCoordinatesString(false));
        locationMessage.setColor(ChatColor.GREEN);

        // Add the location message to the output TextComponent
        output.addExtra(locationMessage);
        output.addExtra(" ");

        // Send the constructed output TextComponent to the player
        player.spigot().sendMessage(output);
    }
}
