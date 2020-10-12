# Playing

For more details on how to play Terasology, keep reading!

If you hit issues look for the game log files. By default they go into your user home directory, for instance `C:\Users\[username]\Saved Games\Terasology\logs` on Windows.

You can run the game with `-homedir` to instead store all game data in the directory you ran from. Or just use the [Launcher](https://github.com/MovingBlocks/TerasologyLauncher/releases) - it handles all that stuff!

Report issues on [GitHub](https://github.com/MovingBlocks/Terasology/issues/new) or ask on [Discord](https://discord.com/channels/270264625419911192/713903952764207164).

## Controls

Note: Keys between the latest stable and latest develop build may differ.

* [W,A,S,D] - Movement
* [E] - Activate / Use (while pointing at a chest, TNT blocks, etc)
* [Q] - Throw held (block) item (hold down to charge for a longer throw!)
* [R] - Keep moving forward (like W) until [R] is pressed again
* [Space] - Jump / Ascend
* [Ctrl] - Crouch / Descend
* [Shift] - Hold to run (or walk when run is toggled to default)
* [Caps lock] - Toggle default between run or walk (starts on run)
* [Left click] - Trigger left click action (default = remove block)
* [Right click] - Trigger right click action (default = place block)
* [Mouse wheel up/down] - Cycle through toolbar slots OR pick up / deposit items into stacks in an inventory one at a time
* [1..0] - Change the active toolbar slot
* [I] - Toggle inventory screen
* [B] - Show infinite block inventory (requires "BlockPicker" module active)
* [C] - Show character screen for stats and equipment (requires "Equipment" module active)
* [H] - Hide user interface
* [T] - Toggle chat interface (effectively a mini-console that only does chat)
* [Tab] - Auto-completion if in the console
* [Tab] - Tab through widgets if not in the console 
* [Shift + Tab] - Tab through widgets in reverse if not in the console 
* [Control + Tab] - Cycle between in-game windows 
* [Home] - Increase viewing distance
* [End] - Decrease viewing distance
* [Escape] - Show/hide the game menu screen
* [F1] OR [`] - Toggle full developer console (the "grave" key, usually above tab)
* [F2] - Show online players overlay 
* [F3] - Toggle debug mode and information
* [F5] - Show behavior tree editor
* [F10] - Show NUI editor
* [F11] - Toggle window focus and reveals a debug pane (only contains stuff if module(s) using it is enabled) 
* [F12] - Take screenshot (goes to /screenshots in game data dir)


## Debug Features

Only works when the F3 debug mode is enabled (and may come and go)

* [Arrow up/down] - Adjust the current time in small steps
* [Arrow left/right] - Adjust the current time in larger steps
* [F4] - Cycle advanced debug metrics
* [F6] - Debug rendering enabled
* [F7] - Cycle debug rendering stage
* [F8] - Debug render chunk bounding boxes
* [F9] - Debug render wire-frame

## Game console

Press the `F1` or `grave` key (usually the \` key immediately above `tab`) to show the in-game console. Mostly everything is case insensitive. Copy paste is supported and up/down arrow will cycle through commands you've used before. Hitting `tab` with a partially typed command will auto-complete it (including abbreviated camel case like lS for listShapes). For partial commands with multiple completion candidates you can `tab` again to cycle through them.

* `help` - Show in-game help (more thorough)
* `search [something]` - searches for any command, prefab, or asset with "something" in its name, help text, etc
* `flight` - just what it sounds like :)
* `ghost` - no-clip mode (fly through anything)
* `hspeed` - greatly increase your movement speed
* `hjump` - jump really high. Almost like flying - but it isn't. Just jump good.
* `restoreSpeed` - normalizes speed (both horizontal and vertical)
* `help give` - Shows detailed help on the "give" command
* `give Water` - Gives 16 water blocks (default quantity when not specified)
* `give Stone 99 Stair` - Gives you 99 stone stair blocks
* `give Chest` - Gives you a Chest block you can place, activate ('E'), put stuff in, break, pick up, place elsewhere, find same stuff in it!
* `give TNT` - Gives you 16 TNT blocks you can place and activate ('E') to blow up
* `bulkGiveBlock cake` - Gives you a stack of blocks for every block with the name "cake" in it! Find good block series with `search`
* `listBlocks` - Lists all actively used blocks (have been loaded for the world)
* `listFreeShapeBlocks` - Lists all blocks that can be requested in any known shape
* `listShapes` - Lists the available shapes
* `healthMax` - Fully restores the player's health
* `showHealth` - Shows the player's health
* `teleport 42 42 42` - Warps the player to x = 42, y = 42, z = 42
* `toggleSunHalting X` - Stops the sun's movement at X position, denoted in days.


## Multiplayer

_A note on stability: while generally Terasology multiplayer is stable enough for us to run play tests on the first Saturday of each month there are regularly recurring bugs that will get in the way (still "Alpha" level software after all). Expect for it to be difficult to keep a server stable over the long term until Beta. The game client may also leave an orphaned process after trying to exit that you may need to close manually via task manager._ 

To name yourself for a multiplayer game use Settings / Player. You can also pick a color for your name in chat and floating name tag.

You can host a local server using the game client and have friends connect to your IP. Game port is 25777 which needs to be open and forwarded to your PC.

Unlike in single player in a multiplayer setting permissions are enforced for console commands. Several permission types are available, if a command is not allowed it will state which permission is missing.

### Headless Server

You can also run a headless server, but need to do so via command line, for example with a downloaded version:

`java -jar libs/Terasology.jar -headless -homedir=server`

This will launch the server and store game files in the "server" subdir at the place you launch from (otherwise it'll use the default path, which could clash with a client on the same system). You can add `-serverPort=#####` to run on a different port than default 25777.

In this case *there is no default player with rights beyond "chat"*. You need to gain admin powers yourself using the `oneTimeAuthorizationKey` that generates in the server's `config.cfg`. This gives you all permission types except "debug"

Join the server and run the console command `usePermissionKey <key>` where you replace `<key>` with the value from the server's config file. This only works once.

After you have rights to manage *user* permissions you can grant other players specific permissions by executing `givePermission <player> <permission>` in the console, replacing `<player>` with the desired player's name (case sensitive).

With *server* rights you can terminate the server gracefully via `shutdownServer` in the console. Otherwise you can kill a headless server with `CTRL-C` in a terminal / command prompt. Running headless with the .exe on Windows is not recommended as it "detaches" from its command prompt so you get no handy logging there or any way to issue a break to the process. If you cannot connect or get "op" you may have to terminate the process manually via Task Manager or comparable.

#### Configuring a server

Finally to get modules configured for a headless server you either have to manually edit in a list of modules to the `defaultModSelection` section, and `defaultGenerator` for your chosen world, then delete the `saves` dir for the server and restart it. Start a single player world and look at the `config.cfg` that generates for hints.

It is also possible to provide a configuration file which overrides the default configuration on startup. This file can be passed using the `-overrideDefaultConfig=<pathToFile>`. Settings from this file will be copied to the generated `config.cfg`. After that changes in the `config.cfg` have priority, so further modifications in the override file will not modify the server values. It not required to pass an entire configuration file. A typical example would be:

```
{
  "defaultModSelection": {
    "modules": [
      "MyModule",
      "MyModule2"
    ],
    "defaultGameplayModuleName": "MyModule"
  },
  "worldGeneration": {
    "worldTitle": "World Title",
    "defaultSeed": "custom seed for the server",
    "defaultGenerator": "MyModule:MyWorldgen"
  },
  "moduleConfigs": {},
}
```

To set the gameplay module, the world generator and some basic server settings.

If you include the "CheatsForAll" module in config for a server then you can bypass a lot of the admin setup as every player will be able to use `cheat` commands, like `give` - however, keep in mind then everybody can cheat, this is really more for testing :-)

Alternatively you can run from source and supply parameters for game configuration. For instance here is how you would launch with ThroughoutTheAges active, our most complete setting. Keep in mind the module list may change any day, check in the game client what modules highlight with TTA selected to confirm.

`gradlew -PworldGen="WoodAndStone:throughoutTheAges" -PextraModules="AlterationEffects,AnotherWorld,AnotherWorldPlants,ClimateConditions,CopperAndBronze,Core,Crops,Fences,Fluid,Genome,GrowingFlora,Hunger,Journal,MarkovChains,MultiBlock,PlantPack,NameGenerator,Seasons,StructuralResources,ThroughoutTheAges,WoodAndStone,Workstation" server`

This will all become easier as the project and especially the launcher mature further :-)

### Server Whitelist and Blacklist

Hosting a server will create a whitelist and a blacklist that can be used to manage who is able to connect to that server.

If the whitelist contains at least one client ID, only the ID(s) on the list will be allowed to connect to the server. All IDs not on the whitelist are effectively blacklisted.

If the whitelist is empty, any ID not on the blacklist will be able to connect. 

Client IDs are added to the lists in JSON format, for example: ["6a5f11f7-4038-4ef0-91ac-86cb957588b1","01264d12-27cf-4699-b8e2-bdc92ac8ef73"]

### Server via Docker

If you're into Docker you can launch a container with a Terasology server already hooked up. 

[qwc](https://github.com/qwc) maintains the related setup and info at https://github.com/qwc/docker-terasology (related: #1749)

See all available images at https://hub.docker.com/r/qwick/terasology/tags/

Example: `docker pull qwick/terasology:latest`

The Docker server starts with no extra modules configured, you would have to go update the game server's config accordingly if desired.
