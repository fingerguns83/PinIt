package net.fg83.pinit.tasks;

import net.fg83.pinit.Pin;
import net.fg83.pinit.PinIt;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.sql.Statement;

public class WarpExpiryTask implements Runnable{
    final PinIt plugin;
    final Player player;
    final Pin pin;
    final int warpId;

    public WarpExpiryTask(PinIt plugin, Player player, Pin pin, int warpId){
        this.plugin = plugin;
        this.player = player;
        this.pin = pin;
        this.warpId = warpId;
    }

    @Override
    public void run() {
        try {
            Statement statement = plugin.connection.createStatement();
            if (statement.executeUpdate("DELETE FROM warps WHERE id=" + warpId) == 1){
                plugin.sendPinItMessage(player, "Your warp to [" + pin.getName() + "] has expired.", false);
            }
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }
}
