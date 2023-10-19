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

/*
Copyright (C) 2023 fingerguns83

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/