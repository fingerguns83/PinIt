# PinIt

## Additional Setup Instructions

The config file covers most things about setup, but here's just a little more info.

### MySQL/MariaDB
PinIt won't automatically make a user or database for you, so just be sure to do that ahead of time.

### Velocity Compatibility
When using PinIt with Velocity, you'll need to install the plugin on every server where you want it. **It does not need to be installed on the proxy server**.

**You must use MySQL/MariaDB for inter-server data sharing.** However, you can always run the plugin in standalone "non-velocity" mode on servers within a Velocity network, but pins created on one server won't be available on others and you won't be able to warp between servers.

Double and triple check that your `server-name` config is set correctly on each server, or else you may have problems with inter-server warping. There *shouldn't* be any problems with having two worlds on different servers with the same name...but, put it this way, I didn't design it with that in mind. Besides, that'd be super confusing for players.

On launch, the plugin will re-scan the server for new worlds and update the database. It will then do this again every 5 minutes. What this means is that the first time you start up multiple servers, it could take a few minutes for earlier servers to become aware of the worlds present on servers that started after it. Same goes for creating new worlds on a server (e.g. Multiverse)--it will take up to five minutes for all servers to discover that new world. If you wish to speed up the process, simply restart the servers.

### LuckPerms Integration
If you have luckperms installed on your server, you'll see a "LuckPerms Integration" section auto-populate in your config. Here, you can elect to override the `personal-pin-limits` config and assign pin count limits based on permission group. If you don't wish to give a particular group any special consideration, just leave it set to 0 (no use deleting it, it'll just re-populate on next startup). If a player belongs to multiple permission groups, they will get the highest limit among those groups.

### Multiverse Integration
PinIt no longer directly interfaces with Multiverse Core--but it is still fully compatible. No special configuration is necessary to use PinIt with MV Core.

## Commands

    pinit:  
      description: Save a quick pin.  
      usage: /pinit <category> <name>  
      permission: pinit.use  
    makepin:  
      description: Make a new pin.  
      usage: /makepin <world> <x> <y> <z> <category> <name>  
      permission: pinit.use  
    pinlist:  
      description: Fetch a list of all pins, sorted by world and/or category if desired.  
      usage: /pinlist [me|server] [world] [category]  
      permission: pinit.use  
    deletepin:  
      description: Delete the specified pin. (Accessed by GUI)  
      usage: /deletepin <pinId>  
      permission: pinit.use  
    sharepin:  
      description: Send pin to another player (Accessed by GUI)  
      usage: /sharepin <pinId> <player>  
      permission: pinit.use.share  
    findpin:  
      description: Search for a pin  
      usage: /findpin <query>  
      permission: pinit.use  
    serverpinit:  
      description: Save a quick server pin.  
      usage: /serverpinit <category> <name>  
      permission: pinit.server.add  
    makeserverpin:  
      description: Make a new server pin.  
      usage: /serverpin <world> <x> <y> <z> <category> <name>  
      permission: pinit.server.add  
    deleteserverpin:  
      description: Delete the specified pin from the server list. (Accessed by GUI)    
      usage: /deleteserverpin <pinId>  
      permission: pinit.server.delete  
    deathpin:  
      description: Get the pin of your last death. Players with "pinit.server.deathpins" can add a player argument to get another players' last death pin.  
      usage: /deathpin [player]
      permission: pinit.use  
    pinwarp:  
      description: Teleport player to pin. (Accessed by GUI)  
      usage: /pinwarp <server|me> <pinId>  
      permission: pinit.warp

## Permissions

    pinit:  
      default: op  
      children:  
        pinit.warp:
          description: Allow player to warp to pins.  
          default: op  
        pinit.server:  
          description: Allow player to control server pins  
          children:  
            pinit.server.add:  
              description: Allow player to add server pins  
            pinit.server.delete:  
              description: Allow player to delete server pins  
            pinit.server.deathpins:  
              description: Allow player to see other players' death pins.  
        pinit.use:
          description: Allow players to make and view pins.  
          default: true  
          children:  
            pinit.use.share:
              description: Allow player to share pins with other players.  
              default: true
