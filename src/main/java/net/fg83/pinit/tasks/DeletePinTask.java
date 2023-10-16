package net.fg83.pinit.tasks;

import net.fg83.pinit.PinIt;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeletePinTask implements Runnable {
    final PinIt plugin;
    final Command command;
    final Player player;
    final int pinId;
    final String statementText;

    public DeletePinTask(PinIt plugin, Command command, Player player, int pinId){
        this.plugin = plugin;
        this.command = command;
        this.player = player;
        this.pinId = pinId;

        if (command.getName().equalsIgnoreCase("deletepin")){
            this.statementText = "DELETE FROM player" + player.getUniqueId().toString().replace("-", "") + " WHERE id = ?";
        }
        else {
            this.statementText = "DELETE FROM global_pins WHERE id = ?";
        }
    }

    @Override
    public void run() {
        try {
            PreparedStatement statement = plugin.connection.prepareStatement(statementText);
            statement.setInt(1, pinId);
            statement.executeUpdate();

            plugin.sendPinItMessage(player, "Pin deleted successfully.", false);
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }
}
