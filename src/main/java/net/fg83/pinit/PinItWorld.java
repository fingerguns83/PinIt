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