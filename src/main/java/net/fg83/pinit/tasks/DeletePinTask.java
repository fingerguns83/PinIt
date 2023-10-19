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