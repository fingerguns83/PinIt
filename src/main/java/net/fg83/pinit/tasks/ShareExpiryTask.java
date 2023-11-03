package net.fg83.pinit.tasks;

import net.fg83.pinit.PinIt;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class ShareExpiryTask implements Runnable {
    PinIt plugin;
    Player player;
    String targetPlayer;
    int shareId;

    public ShareExpiryTask(PinIt plugin, Player player, String target, int shareId){
        this.plugin = plugin;
        this.player = player;
        this.targetPlayer = target;
        this.shareId = shareId;
    }

    @Override
    public void run() {
        try {
            Statement statement = plugin.connection.createStatement();
            if (statement.executeUpdate("DELETE FROM shares WHERE id=" + shareId) == 1){
                plugin.sendPinItMessage(player, "Your share to [" + targetPlayer + "] could not be delivered.", false);
            }
            else {
                plugin.sendPinItMessage(player, "Shared pin successfully!", false);
            }
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }
}
