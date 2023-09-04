package net.fg83.pinit.commands;

import net.fg83.pinit.PinIt;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeletePinCommand implements CommandExecutor {

    PinIt plugin;
    public DeletePinCommand(PinIt plugin) { this.plugin = plugin; }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            return false;
        }
        Player player = (Player) sender;

        if (args.length < 1){
            return false;
        }
        int pinId = 0;
        try {
            pinId = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e){
            plugin.sendPinItMessage(player, "Invalid argument! Usage: \"/deletepin <pinId>\"", true);
            return false;
        }
        if (pinId < 1){
            plugin.sendPinItMessage(player, "Invalid argument! Usage: \"/deletepin <pinId>\"", true);
            return false;
        }
        int finalPinId = pinId;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String input = "DELETE FROM player" + player.getUniqueId().toString().replace("-", "") + " WHERE id = ?";
                PreparedStatement statement = plugin.connection.prepareStatement(input);
                statement.setInt(1, finalPinId);
                statement.executeUpdate();

                plugin.sendPinItMessage(player, "Pin deleted successfully.", false);
            }
            catch (SQLException e){
                plugin.getLogger().info("Database error: {" + e.getMessage() + "}");
            }
        });
        return true;
    }
}
