package net.fg83.pinit;

public class PinItWorld {
    private final String worldId;
    private final String name;
    private final String server;

    public PinItWorld(String worldId, String name, String server){
        this.worldId = worldId;
        this.name = name;
        this.server = server;
    }

    public String getWorldId(){
        return this.worldId;
    }
    public String getFancyName() {
        return this.name;
    }
    public String getSafeName(){
        return this.name.replace(" ", "-");
    }
    public String getServer(){
        return this.server;
    }
}
