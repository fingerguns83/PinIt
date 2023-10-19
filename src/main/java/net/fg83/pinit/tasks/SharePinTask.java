package net.fg83.pinit.tasks;

import net.fg83.pinit.Pin;
import net.fg83.pinit.PinIt;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SharePinTask implements Runnable{
    final PinIt plugin;
    final Player player;
    final Player target;
    final int pinId;

    public SharePinTask(PinIt plugin, Player player, Player target, int pinId){
        this.plugin = plugin;
        this.player = player;
        this.target = target;
        this.pinId = pinId;
    }
    @Override
    public void run() {
        // Prepare the SQL query to select a pin by id from the player's table
        String input = "SELECT * FROM player" + player.getUniqueId().toString().replace("-", "") + " WHERE id = ?";

        try {
            // Prepare a PreparedStatement with the query
            PreparedStatement statement = plugin.connection.prepareStatement(input);

            // Set the value of the placeholder in the query with pinId\
            statement.setInt(1, pinId);

            // Execute the query and get the result set
            ResultSet resultSet = statement.executeQuery();

            // Check if the result set has a next row
            if (resultSet.next()){
                // Create a new Pin object from the result set
                Pin pin = new Pin(resultSet, player, plugin, false);

                TextComponent confirmation = new TextComponent("Shared ");
                confirmation.setColor(ChatColor.WHITE);
                confirmation.setBold(false);

                TextComponent pinName = new TextComponent("[" + pin.getName() + "] ");
                pinName.setColor(ChatColor.DARK_AQUA);
                pinName.setBold(false);

                TextComponent confEnd = new TextComponent("with " + target.getName() + "!");
                confEnd.setColor(ChatColor.WHITE);
                confEnd.setBold(false);

                confirmation.addExtra(pinName);
                confirmation.addExtra(confEnd);

                plugin.sendPinItMessage(player, confirmation);

                pin.sendShareMessage(target);
            }
            else {
                // Inform the player about the invalid pin
                plugin.sendPinItMessage(player, "Invalid pin. Try using the share button from \"/pinlist\"", true);
            }
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }
}
