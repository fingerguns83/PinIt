package net.fg83.pinit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class PersonalPin extends Pin{
    private Player player;

    public PersonalPin(Player player, String name, Location location, PinIt plugin) {
        super(name, location, plugin);
        this.player = player;
    }

    public void store(){
        plugin.createPlayerTable(player);

        String input = "INSERT INTO player" + player.getUniqueId().toString().replace("-", "") + " (name, location_world, locationX, locationY, locationZ) VALUES (?, ?, ?, ?, ?)";
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

    @Override
    public TextComponent makeShareButton(){
        TextComponent shareMessage = new TextComponent("[+]");
        shareMessage.setColor(ChatColor.LIGHT_PURPLE);

        shareMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sharepin " + pinId + " "));

        TextComponent hoverText = new TextComponent("Send this pin to another player.");
        hoverText.setItalic(true);
        BaseComponent[] hoverComponents = new BaseComponent[]{hoverText};
        shareMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));

        return shareMessage;
    }

    @Override
    public TextComponent makeDeleteButton(boolean canDelete){
        TextComponent deleteMessage = new TextComponent("[x]");
        deleteMessage.setColor(ChatColor.DARK_RED);
        deleteMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/deletepin " + pinId));

        TextComponent hoverText = new TextComponent("Delete this pin.");
        hoverText.setItalic(true);
        BaseComponent[] hoverComponents = new BaseComponent[]{hoverText};
        deleteMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));
        deleteMessage.addExtra(" ");
        return deleteMessage;
    }
    @Override
    public TextComponent makeTitle(){
        return new TextComponent(this.name);
    }

    public void sendShareMessage(Player target, PinIt plugin){
        TextComponent output = new TextComponent();

        TextComponent nameMessage = new TextComponent("*" + this.name);
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
        output.addExtra(" ");

        TextComponent acceptMessage = new TextComponent("[+]");
        acceptMessage.setColor(ChatColor.DARK_GREEN);
        String addPinCommand = "/pinit " + plugin.getNamespacedWorld(location.getWorld().getName(), true) +
                " " + getLocationString(true) + " " + this.name;
        acceptMessage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, addPinCommand));

        TextComponent hoverText = new TextComponent("Add this pin to your pin list.");
        hoverText.setItalic(true);
        BaseComponent[] hoverComponents = new BaseComponent[]{hoverText};
        acceptMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents));
        output.addExtra(acceptMessage);

        plugin.sendPinItMessage(target, player.getName() + " has shared a pin with you!", false);
        target.spigot().sendMessage(output);
    }
}
