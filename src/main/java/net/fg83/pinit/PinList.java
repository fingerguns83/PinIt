package net.fg83.pinit;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PinList {

    PinIt plugin;
    Player player = null;
    List<Pin> pins = new ArrayList<>();
    private int page;
    private boolean hasNextPage = false;
    private boolean hasPrevPage = false;

    public PinList(PinIt plugin, @Nullable Player player){
        this.plugin = plugin;
        this.player = player;
    }

    private String makeFetchStatement(int pageNo, @Nullable Player player, @Nullable String world){
        String fetchStatement = "";
        if (player != null){
            fetchStatement = "SELECT * FROM player" + player.getUniqueId().toString().replace("-", "");
        }
        else {
            fetchStatement = "SELECT * FROM server";
        }
        if (world != null){
            fetchStatement = fetchStatement.concat(" WHERE location_world='" + world + "'");
        }
        fetchStatement = fetchStatement.concat(" ORDER BY LOWER(name), name LIMIT 10");
        if (pageNo > 1){
            int offset = (pageNo - 1) * 10;
            fetchStatement = fetchStatement.concat(" OFFSET " + offset);
        }
        return fetchStatement;
    }
    private int countRecords(@Nullable Player player, @Nullable String world){
        String input = "";
        if (player != null){
            input = "SELECT COUNT(*) FROM player" + player.getUniqueId().toString().replace("-", "");
        }
        else {
            input = "SELECT COUNT(*) FROM server";
        }
        if (world != null){
            input = input.concat(" WHERE location_world='" + world + "'");
        }

        int recordCount = 0;
        try {
            Statement statement = plugin.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(input);
            if (resultSet.next()){
                recordCount = resultSet.getInt(1);
            }
            resultSet.close();
            statement.close();
        }
        catch (SQLException e){
            plugin.getLogger().info(e.getMessage());
        }

        return recordCount;
    }
    public void populate(int pageNo, @Nullable String world, @Nullable Player player){
        this.page = pageNo;
        if (pageNo > 1){
            this.hasPrevPage = true;
        }

        String fetchStatement = makeFetchStatement(pageNo, player, world);

        // Check for additional pages
        int recordCount = countRecords(player, world);

        if ((pageNo * 10) < recordCount){
            hasNextPage = true;
        }

        // Fetch list with constraints
        try {
            Statement statement = plugin.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(fetchStatement);
            while (resultSet.next()) {

                int pinId = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String pinWorld = resultSet.getString("location_world");
                int locationX = resultSet.getInt("locationX");
                int locationY = resultSet.getInt("locationY");
                int locationZ = resultSet.getInt("locationZ");

                Location location = new Location(plugin.getServer().getWorld(pinWorld), locationX, locationY, locationZ);
                if (player != null) {
                    PersonalPin pin = new PersonalPin(player, name, location, plugin);
                    pin.setPinId(pinId);
                    pins.add(pin);
                }
                else {
                    Pin pin = new Pin(name, location, plugin);
                    pin.setPinId(pinId);
                    pins.add(pin);
                }

            }
        }
        catch (SQLException e){
            plugin.getLogger().info("Database error: {" + e.getMessage() + "}");
        }
    }

    public List<Pin> getPins() {
        return pins;
    }
    public boolean hasNext(){
        return hasNextPage;
    }
    public boolean hasPrev(){
        return hasPrevPage;
    }
    public int getNext(){
        return page + 1;
    }
    public int getPrev(){
        return page - 1;
    }
    public int getPage() {
        return page;
    }
}
