package net.fg83.pinit.tasks;

import net.fg83.pinit.PinIt;

import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUpdateTask implements Runnable{
    final PinIt plugin;
    final String input;

    public DatabaseUpdateTask(PinIt plugin, String input){
        this.plugin = plugin;
        this.input = input;
    }

    @Override
    public void run() {
        try {
            Statement statement = plugin.connection.createStatement();
            statement.executeUpdate(input);
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }
}
