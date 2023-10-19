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
