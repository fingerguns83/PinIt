package net.fg83.pinit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.Objects;

public class Pin {

    int pinId;
    String name;
    Location location;
    PinIt plugin;

    public Pin(String name, Location location, PinIt plugin){
        this.name = name;
        this.location = location;

        this.plugin = plugin;
    }

    public void setPinId(int pinId){
        this.pinId = pinId;
    }
    public int getPinId() {
        return pinId;
    }
    public String getName() {
        return name;
    }
    public Location getLocation() {
        return location;
    }
    public String getLocationString(boolean teleport){

        if (teleport){
            return location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
        }
        else {

            return "(" + plugin.getNamespacedWorld(location.getWorld().getName(), true) + ") " +
                    location.getBlockX() + ", " +
                    location.getBlockY() + ", " +
                    location.getBlockZ();
        }
    }

    public void store(){
        String input = "INSERT INTO server (name, location_world, locationX, locationY, locationZ) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = plugin.connection.prepareStatement(input);
            statement.setString(1, name);
            statement.setString(2, Objects.requireNonNull(location.getWorld()).getName());
            statement.setInt(3, location.getBlockX());
            statement.setInt(4, location.getBlockY());
            statement.setInt(5, location.getBlockZ());
            statement.executeUpdate();
        }
        catch (SQLException e){
            plugin.getLogger().info("Database error: {" + e.getMessage() + "}");
        }
    }

    public TextComponent makeShareButton(){
        TextComponent shareMessage = new TextComponent("[+]");
        shareMessage.setColor(ChatColor.LIGHT_PURPLE);

        String addPinCommand = "/pinit " + plugin.getNamespacedWorld(location.getWorld().getName(), true) +
                " " + getLocationString(true) + " " + this.name;
        shareMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, addPinCommand));

        TextComponent hoverText = new TextComponent("Add this pin to your pin list.");
        hoverText.setItalic(true);
        BaseComponent[] hoverComponents = new BaseComponent[]{hoverText};
        shareMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));

        return shareMessage;
    }

    public TextComponent makeDeleteButton(boolean canDelete){
        TextComponent output = new TextComponent();
        if (canDelete) {
            TextComponent deleteMessage = new TextComponent("[x]");
            deleteMessage.setColor(ChatColor.DARK_RED);
            deleteMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/delserverpin " + pinId));

            TextComponent hoverText = new TextComponent("Delete this pin.");
            hoverText.setItalic(true);
            BaseComponent[] hoverComponents = new BaseComponent[]{hoverText};
            deleteMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));
            deleteMessage.addExtra(" ");
            output.addExtra(deleteMessage);
        }
        return output;
    }
    public TextComponent makeTitle(){
        return new TextComponent("*" + this.name);
    }
    public void sendMessage(Player player, PinIt plugin, boolean hasPermission){
        TextComponent output = new TextComponent();

        output.addExtra(makeDeleteButton(hasPermission));

        output.addExtra(makeShareButton());

        output.addExtra(" ");

        TextComponent nameMessage = makeTitle();
        nameMessage.setColor(ChatColor.GOLD);
        nameMessage.setItalic(true);
        output.addExtra(nameMessage);

        output.addExtra(": ");

        TextComponent locationMessage = new TextComponent(this.getLocationString(false));
        locationMessage.setColor(ChatColor.GREEN);
        if (player.hasPermission("pinit.warp")){
            locationMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/execute in " + plugin.getNamespacedWorld(location.getWorld().getName(), false) + " run tp @s " + this.getLocationString(true)));
        }
        output.addExtra(locationMessage);

        player.spigot().sendMessage(output);
    }
}
