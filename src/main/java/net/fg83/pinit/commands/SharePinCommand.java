package net.fg83.pinit.commands;

import net.fg83.pinit.PersonalPin;
import net.fg83.pinit.PinIt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SharePinCommand implements CommandExecutor {
    PinIt plugin;
    public SharePinCommand(PinIt plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            return false;
        }
        Player player = (Player) sender;

        if (args.length != 2){
            plugin.sendPinItMessage(player, "Invalid command! Usage: \"/sharepin <pinId> <player>\"", true);
            return false;
        }
        int pinId = 0;
        try {
            pinId = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e){
            plugin.sendPinItMessage(player, "Invalid pinId. Try using the share button from \"/pinlist\".", true);
            return false;
        }
        if (pinId < 1){
            plugin.sendPinItMessage(player, "Invalid pinId. Try using the share button from \"/pinlist\".", true);
            return false;
        }

        Player target = plugin.getServer().getOnlinePlayers().stream().filter(onlinePlayer -> onlinePlayer.getName().equalsIgnoreCase(args[1])).findFirst().orElse(null);

        if (target == null) {
            plugin.sendPinItMessage(player, "Player is either offline or does not exist.", true);
            return false;
        }

        // Send target player message
        int finalPinId = pinId;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String input = "SELECT * FROM player" + player.getUniqueId().toString().replace("-", "") + " WHERE id = ?";
            try {
                PreparedStatement statement = plugin.connection.prepareStatement(input);
                statement.setInt(1, finalPinId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()){
                    String name = resultSet.getString("name");
                    String pinWorld = resultSet.getString("location_world");
                    int locationX = resultSet.getInt("locationX");
                    int locationY = resultSet.getInt("locationY");
                    int locationZ = resultSet.getInt("locationZ");

                    Location location = new Location(plugin.getServer().getWorld(pinWorld), locationX, locationY, locationZ);
                    PersonalPin pin = new PersonalPin(player, name, location, plugin);
                    pin.sendShareMessage(target, plugin);
                }
                else {
                    plugin.sendPinItMessage(player, "Invalid pin. Try using the share button from \"/pinlist\"", true);
                }
            }
            catch (SQLException e){
                plugin.getLogger().info(e.getMessage());
            }
        });
        return true;
    }
}
