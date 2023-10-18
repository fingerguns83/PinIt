package net.fg83.pinit;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class TagList {
    final PinIt plugin;
    private Player owner = null;
    private final List<String> tags = new ArrayList<>();

    public TagList(@Nullable Player player, PinIt plugin){
        this.plugin = plugin;

        if (player != null){
            this.owner = player;
        }

        refresh();
    }

    public List<String> getTags(){
        return tags;
    }

    public void refresh() {
        tags.clear();

        try {
            Statement statement = plugin.connection.createStatement();

            ResultSet resultSet;
            if (owner != null){
                resultSet = statement.executeQuery("SELECT DISTINCT category FROM player" + owner.getUniqueId().toString().replace("-", ""));
            }
            else {
                resultSet = statement.executeQuery("SELECT DISTINCT category FROM global_pins");
            }
            while(resultSet.next()){
                tags.add(resultSet.getString("category"));
            }
            statement.close();
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }
    }
}
