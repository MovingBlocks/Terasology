Terasology
==========

![Terasology](/engine/src/main/resources/assets/textures/menuBackground.jpg "Terasology")

Terasology is a game that pays ample tribute to [Minecraft](http://www.minecraft.net) in initial look and origin, but stakes out its own niche by aiming for the NPC-helper and caretaker focus from such games as [Dwarf Fortress](http://www.bay12games.com/dwarves) and [Dungeon Keeper](http://en.wikipedia.org/wiki/Dungeon_Keeper), while striving for added depth and sophistication.

Terasology is an open source project started by Benjamin "begla" Glatzel to research procedural terrain generation and efficient rendering techniques in Java using the [LWJGL](http://lwjgl.org). The engine uses a block-based voxel-like approach as seen in Minecraft. You can check out his blog at [Moving Blocks!](http://blog.movingblocks.net)

The creators of Terasology are a diverse mix of software developers, game testers, graphic artists, and musicians. Get involved by checking out our [Community Portal](http://forum.terasology.org/index.php), [Facebook Page](http://www.facebook.com/pages/Terasology/248329655219905), [Twitter](https://twitter.com/Terasology), [G+](https://plus.google.com/b/103835217961917018533), or [Reddit](http://www.reddit.com/r/Terasology)

Terasology is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html) and available in source code form at [GitHub](https://github.com/MovingBlocks/Terasology).

[![Build Status](http://jenkins.terasology.org/job/Terasology/badge/icon)](http://jenkins.terasology.org/job/Terasology/)
[![Release](https://img.shields.io/github/release/MovingBlocks/Terasology.svg)](../../releases/latest)
[![Downloads](https://img.shields.io/github/downloads/MovingBlocks/Terasology/latest/total.svg "Downloads")](../../releases/latest)
[![Bounties](https://img.shields.io/bountysource/team/MovingBlocks/activity.svg)](https://www.bountysource.com/teams/MovingBlocks)
![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)
[![Dependency Status](https://www.versioneye.com/user/projects/537612b214c1584e82000022/badge.svg)](https://www.versioneye.com/user/projects/537612b214c1584e82000022)

Setup
--------

Terasology requires Java 8, the newer the better - [download it here](http://www.java.com/en/download/manual.jsp). Also make sure that your graphics card driver is up to date.

For easy setup (recommended) you can use our launcher - [download it here](https://github.com/MovingBlocks/TerasologyLauncher/releases) which offers different release lines - for instance stable, develop, and legacy

Direct download stable builds are uploaded to [our release section here on GitHub](https://github.com/MovingBlocks/Terasology/releases) while the cutting-edge develop version can be downloaded direct [here from our Jenkins](http://jenkins.terasology.org/job/DistroOmega/lastSuccessfulBuild/artifact/distros/omega/build/distributions/TerasologyOmega.zip)

You can use the Windows executable or one of the default launch scripts to start the game. They will setup your Java Virtual Machine to allocate up to 1024 MB of memory. Under Linux and Mac OS X the run script needs the access permission "Execute" to run properly: "chmod +x [scriptname].sh".

The game auto-saves at regular intervals so it is decently crash-hardened at this point (no more playing for 20 minutes then poof lost world via crash). Still no guarantees though! Pre-Alpha :-)

Multiplayer
--------

To name yourself for a multiplayer game use Settings / Player. You can also pick a color, which will affect your placeholder monkey head player avatar, name in chat, and floating name tag.

You can host a local server using the game client and have friends connect to your IP. Game port is 25777 which needs to be open and forwarded to your PC.

Unlike in single player in a multiplayer setting permissions are enforced for console commands. Several permission types are available, if a command is not allowed it will state which permission is missing.

You can also run a headless server, but this is harder to configure at the moment. You need to launch the game via command line, for example with a downloaded version:

*Note: This changed to include the `/libs` after stable 49. The name of the .exe also may differ*

`java -jar libs/Terasology.jar -headless -homedir=server`

This will launch the server and store game files in the "server" subdir at the place you launch from (otherwise it'll use the default path, which could clash with a client on the same system). You can add `-serverPort=#####` to run on a different port than default 25777.

In this case *there is no default player with rights beyond "chat"*. You need to gain admin powers yourself using the `oneTimeAuthorizationKey` that generates in the server's `config.cfg`. This gives you all permission types except "debug"

Join the server and run the console command `usePermissionKey <key>` where you replace `<key>` with the value from the server's config file. This only works once.

After you have rights to manage *user* permissions you can grant other players specific permissions by executing `givePermission <player> <permission>` in the console, replacing `<player>` with the desired player's name (case sensitive).

With *server* rights you can terminate the server gracefully via `shutdownServer` in the console. Otherwise you can kill a headless server with `CTRL-C` in a terminal / command prompt. Running headless with the .exe on Windows is not recommended as it "detaches" from its command prompt so you get no handy logging there or any way to issue a break to the process. If you cannot connect or get "op" you may have to terminate the process manually via Task Manager or comparable.

Finally to get modules configured for a headless server you either have to manually edit in a list of modules to the `defaultModSelection` section, and `defaultGenerator` for your chosen world, then delete the `saves` dir for the server and restart it. Start a single player world and look at the `config.cfg` that generates for hints.

If you include the "CheatsForAll" module in config for a server then you can bypass a lot of the admin setup as every player will be able to use `cheat` commands, like `giveBlock` - however, keep in mind then everybody can cheat, this is really more for testing :-)

Alternatively you can run from source and supply parameters for game configuration. For instance here is how you would launch with ThroughoutTheAges active, our most complete setting. Keep in mind the module list may change any day, check in the game client what modules highlight with TTA selected to confirm.

`gradlew -PworldGen="WoodAndStone:throughoutTheAges" -PextraModules="AlterationEffects,AnotherWorld,AnotherWorldPlants,ClimateConditions,CopperAndBronze,Core,Crops,Fences,Fluid,Genome,GrowingFlora,Hunger,Journal,MarkovChains,MultiBlock,PlantPack,NameGenerator,Seasons,StructuralResources,ThroughoutTheAges,WoodAndStone,Workstation" startServer`

This will all become easier as the project and especially the launcher mature further :-)

Controls
--------

Note: Keys between the latest stable and latest develop build may differ.

* [W,A,S,D] - Movement
* [E] - Activate / Use (while pointing at a chest, TNT blocks, etc)
* [Q] - Throw held (block) item (hold down to charge for a longer throw!)
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
* [H] - Hide user interface
* [T] - Toggle chat interface (effectively a mini-console that only does chat)
* [`] - Toggle full developer console (the "grave" key, above tab)
* [Tab] - Auto-completion in the console
* [Home] - Increase viewing distance
* [End] - Decrease viewing distance
* [Escape] - Show/hide the game menu screen
* [F1] - Toggle window focus and reveals a debug pane (only contains stuff if module(s) using it is enabled)
* [F3] - Toggle debug mode and information
* [F5] - Show behavior tree editor
* [F12] - Take screenshot (goes to /screenshots in game data dir)

Debug Features
--------

Only works when the F3 debug mode is enabled (and may come and go)

* [Arrow up/down] - Adjust the current time in small steps
* [Arrow left/right] - Adjust the current time in larger steps
* [F4] - Cycle advanced debug metrics
* [F6] - Debug rendering enabled
* [F7] - Cycle debug rendering stage
* [F8] - Debug render chunk bounding boxes
* [F9] - Debug render wire-frame

Tools
--------

May vary by version and based on what modules are enabled

* Axe / Pickaxe / Shovel - Faster right-click removal of some blocks
* Torch - Shiny! Place with left click. Try throwing these in water at night
* Explosion tool - Big bada boom!
* Railgun  - Bigger bada boom, in a straight line! *Use with caution! May causes severe lag*
* Goodie chest - place it and open with 'e' for assorted goodies

More or completely alternative line-ups with certain modules / world types selected

Console Commands
--------

Press the `grave` key (usually the \` key immediately above `tab`) to show the in-game console. Mostly everything is case insensitive. Copy paste is supported and up/down arrow will cycle through commands you've used before. Hitting `tab` with a partially typed command will auto-complete it (including abbreviated camel case like lS for listShapes). For partial commands with multiple completion candidates you can `tab` again to cycle through them.

* help - Show in-game help (more thorough)
* search [something] - searches for any command, prefab, or asset with "something" in its name, help text, etc
* flight - just what it sounds like :)
* ghost - no-clip mode (fly through anything)
* hspeed - greatly increase your movement speed
* hjump - jump really high. Almost like flying - but it isn't. Just jump good.
* restoreSpeed - normalizes speed (both horizontal and vertical)
* help giveBlock - Shows detailed help on the "giveBlock" command
* giveBlock Water - Gives 16 water blocks
* giveBlock Stone Stair 99 - Gives you 99 stone stair blocks
* giveBlock Chest - Gives you a Chest block you can place, activate ('E'), put stuff in, break, pick up, place elsewhere, find same stuff in it!
* giveBlock TNT - Gives you 16 TNT blocks you can place and activate ('E') to blow up
* listBlocks - Lists all actively used blocks (have been loaded for the world)
* listFreeShapeBlocks - Lists all blocks that can be requested in any known shape
* listShapes - Lists the available shapes
* healthMax - Fully restores the player's health
* showHealth - Shows the player's health
* teleport 42 42 42 - Warps the player to x = 42, y = 42, z = 42

Building and running from source
--------

Run any commands in the project root directory

* Download / clone the source from GitHub
* To prepare for IntelliJ run: `gradlew idea`
* To prepare for Eclipse run: `gradlew eclipse`
* To run from the command line: `gradlew run`
* Start a headless server: `gradlew start` (stores data in /terasology-server - the Gradle command blocks until server is killed)
* For more tasks: `gradlew tasks`

You may also need to tweak IDE settings further for your convenience, in particular for Eclipse. See [Dev Setup](https://github.com/MovingBlocks/Terasology/wiki/Dev-Setup) in our wiki for more details.

Note that by default stored data (config, saves, etc) is sent to a user profile directory like Saved Games under Windows. Add `-homedir` to a run configuration or command line execution to use the project dir instead (this is done automatically in some cases including IntelliJ and `gradlew run`)

If you want to pull down the source code for a module you can easily do so via Gradle:

* `gradlew fetchModuleSample`
* `gradlew idea`

This fetches the module source for the "Sample" module and the second command fetches any dependencies and updates the IntelliJ project structure so you can see it as a module. Likewise for Eclipse or any other setup you should run any one `gradlew` command to make sure the new module's dependencies have been fetched as well. See [Codebase Structure](https://github.com/MovingBlocks/Terasology/wiki/Codebase-Structure) in the wiki for more.

Modules
--------

Content, gameplay mechanics, and mostly everything other than the engine that allows the game to run is stored in what we call "modules" which are _similar_ to the traditional meaning of "mods" but intended to be smaller building blocks you'd normally put several of together to make one traditional "mod".

Modules must be enabled during world creation by selecting them using the "Modules" button. Some world generator types may be registered by a module and auto-select that and maybe other modules if you choose that world type. Modules may also enable additional console commands (listed by the "help" command when active).

As opposed to engine level projects listed under https://github.com/MovingBlocks all modules are listed under a different GitHub organization at https://github.com/Terasology

Here's a list of modules bundled with the game by default (as of this writing anyway - this line-up will change now and then). It should roughly match this category in Jenkins: http://jenkins.terasology.org/view/Modules and you can download updated modules from there if needed.

* [AlterationEffects](https://github.com/Terasology/AlterationEffects) - module for storing some buff/debuff type effects
* [AnotherWorld](https://github.com/Terasology/AnotherWorld) - world gen module, includes features like ore placement and caves, used by WoodAndStone's world
* [AnotherWorldPlants](https://github.com/Terasology/AnotherWorldPlants) - farming and tree growth for AnotherWorld
* [BlockNetwork](https://github.com/Terasology/BlockNetwork) - a framework to support blocks that can communicate with each other in some fashion
* [BlockPicker](https://github.com/Terasology/BlockPicker) - allows the player access to an infinite block inventory with the `B` key
* [Breathing](https://github.com/Terasology/Breathing) - without this you can't breathe! But you also don't need to. Enable it to drown properly in water (or not!)
* [CakeLie](https://github.com/Terasology/CakeLie) - the cake may be a lie, but these cake and candy blocks are delicious!
* [Caves](https://github.com/Terasology/Caves) - a cave generation module (actually an ore vein generator placing air veins!) based on CustomOreGen
* [ChangingBlocks](https://github.com/Terasology/ChangingBlocks) - allows blocks to change over time (such as switching to a more dusty / worn looking block after x amount of days)
* [CheatsForAll](https://github.com/Terasology/CheatsForAll) - if enabled on a server allows the use of `cheat` commands for any player connected without any additional admin setup
* [ChrisVolume1OST](https://github.com/Terasology/ChrisVolume1OST) - official game soundtrack by Chris Köbke - volume 1
* [ChrisVolume2OST](https://github.com/Terasology/ChrisVolume2OST) - official game soundtrack by Chris Köbke - volume 2
* [Cities](https://github.com/Terasology/Cities) - procedural city placer and plot organizer, also places roads to connect cities
* [ClimateConditions](https://github.com/Terasology/ClimateConditions) - A library module for managing temperature, humidity, and other climate factors 
* [CommonWorld](https://github.com/Terasology/CommonWorld) - general world generation utility module
* [ComputerMonitors](https://github.com/Terasology/ComputerMonitors) - allows for the creation of functional multi-block monitors in-game - ModularComputers puts them to work!
* [CopperAndBronze](https://github.com/Terasology/CopperAndBronze) - another era for throughout the ages, this time copper and bronze (comes after wood and stone)
* Core - mandatory content needed for normal game launch
* CoreSampleGameplay - gameplay front for Core - allows modules to depend on Core without the default starting inventory
* [CustomOreGen](https://github.com/Terasology/CustomOreGen) - library containing an ore distribution algorithm based on [JRoush's CustomOreGen](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1282294-1-4-6-v2-custom-ore-generation-updated-jan-5th)
* [Durability](https://github.com/Terasology/Durability) - library module to support destructible items (wear and tear eventually breaks them)
* [EventualSkills](https://github.com/Terasology/EventualSkills) - a time-based skill system, a bit akin to EVE Online's system.
* [Fences](https://github.com/Terasology/Fences) - fences!
* [Fluid](https://github.com/Terasology/Fluid) - adds support for fluid in non-world situations (such as for storage in workstations)
* [FluidComputerIntegration](https://github.com/Terasology/FluidComputerIntegration) - allows for interactions with fluids via computers
* [FunnyBlocks](https://github.com/Terasology/FunnyBlocks) - cheese wheels and bowling pins - why not
* [Genome](https://github.com/Terasology/Genome) - genetics WOO! Complete with DNA letters and mutating plants. Part of the Wood & Stone line-up
* [GrowingFlora](https://github.com/Terasology/GrowingFlora) - organically growing (step by step) trees and such
* [Hunger](https://github.com/Terasology/Hunger) - makes the player slowly gets hungry (needs actual GUI work and ways to then actually eat food though). Console `hungerCheck` for stats
* [InGameHelp](https://github.com/Terasology/InGameHelp) - system for showing help in-game, default key `P`
* [IRLCorp](https://github.com/Terasology/IRLCorp) - Industrialized Reduction of Labor Corporation - Helping workmen everywhere
* [ItemRendering](https://github.com/Terasology/ItemRendering) - a library for displaying "holographic" items in the world
* [JoshariasSurvival](https://github.com/Terasology/JoshariasSurvival) - formerly known as TerraTech - gameplay template for a machine-centric survival style
* [Journal](https://github.com/Terasology/Journal) - allows the player to use an in-game journal for gameplay notifications and such. Default toggle key 'J'
* ~~[LandOfTerra](https://github.com/Terasology/LandOfTerra) - contains a set of unusual world generators~~ (being refactored)
* [LegacyMusic](https://github.com/Terasology/LegacyMusic) - older music pieces predating the official soundtrack
* [LightAndShadow](https://github.com/Terasology/LightAndShadow) - main module for the Light & Shadow gameplay
* [LightAndShadowResources](https://github.com/Terasology/LightAndShadowResources) - IMMA FIRIN’ MAH LASR!! Art assets for the Light & Shadow concept
* [Machines](https://github.com/Terasology/Machines) - machine infrastructure library module
* [Malicious](https://github.com/Terasology/Malicious) - a series of module security tests to check that modules cannot do naughty things when running
* [ManualLabor](https://github.com/Terasology/ManualLabor) - tools and logic for manual labor (digging, chopping, etc)
* [ManualLaborEventualSkills](https://github.com/Terasology/ManualLaborEventualSkills) - bridge module for adding EventualSkills to ManualLabor
* [MarcinScIncubator](https://github.com/Terasology/MarcinScIncubator) - parking lot for tools used in @MarcinSc's many modules without a more explicit home yet
* [MarkovChains](https://github.com/Terasology/MarkovChains) - Library module with some pseudo random math stuff
* [MasterOfOreon](https://github.com/Terasology/MasterOfOreon) - Master the Oreons, or others like them, from the throne-world of the Ancients! A menu command system, default show/hide key 'O'
* [Maze](https://github.com/Terasology/Maze) - a maze generator. Right-click with the provided maze tool on one block then again on another and a maze will generate between the two points (in multiple layers if the area is tall enough)
* [Minerals](https://github.com/Terasology/Minerals) - a large collection of mineral blocks
* [Miniion](https://github.com/Terasology/Miniion) - base creature control system, used by MasterOfOreon - old module that has gone through a few redesigns
* [Minimap](https://github.com/Terasology/Minimap) - a basic minimap. Show/hide with `M` by default and zoom with numpad `-` and `+`
* [MobileBlocks](https://github.com/Terasology/MobileBlocks) - supports blocks that can move their location based on some directions
* [ModularComputers](https://github.com/Terasology/ModularComputers) - central module for the creation of computers that themselves can have "hardware modules" of sorts added to them in-game to add interesting functionality
* [MoreLights](https://github.com/Terasology/MoreLights) - assorted illuminated blocks
* [MultiBlock](https://github.com/Terasology/MultiBlock) - supports the concept of multiple blocks being part of the same structure
* [MusicDirector](https://github.com/Terasology/MusicDirector) - allows music assets to be prepared for dynamic inclusion in appropriate contexts (like time of day)
* [NameGenerator](https://github.com/Terasology/NameGenerator) - can create random themed names for use by other modules, or via console using commands like `generateNameList 10`
* [OreGeneration](https://github.com/Terasology/OreGeneration) - ore generation plugin system based on CustomOreGen (this one enables easy definition of what ores you want in a world)
* [Oreons](https://github.com/Terasology/Oreons) - little sentient cookie people! Don't do much yet. Place with `spawnPrefab OreonGuard` in the console (other types exist)
* [Pathfinding](https://github.com/Terasology/Pathfinding) - framework for pathfinding used by other modules
* [PlantPack](https://github.com/Terasology/PlantPack) - more plants! Used by the Throughout the Ages gameplay
* [PolyWorld](https://github.com/Terasology/PolyWorld) - creates very neat island worlds based on the [map generating algorithm by Amit Patel of Red Blob Games](http://www-cs-students.stanford.edu/~amitp/game-programming/polygon-map-generation/)
* [Portals](https://github.com/Terasology/Portals) - allows placement of portal blocks that'll spawn Oreons `giveBlock portal`
* [QuestExamples](https://github.com/Terasology/QuestExamples) - samples for developers to help create quests
* ~~[Rails](https://github.com/Terasology/Rails) - railroads and trains! Press 'e' to start a caboose or enter a cart. Use the wrench to attach carts~~ (broken, waiting for author's return)
* [Sample](https://github.com/Terasology/Sample) - miscellaneous example content showcasing module usage
* [Seasons](https://github.com/Terasology/Seasons) - adds seasons to the game
* [Signalling](https://github.com/Terasology/Signalling) - circuitry implementation based on BlockNetwork, similar to redstone
* [SimpleFarming](https://github.com/Terasology/SimpleFarming) - easy to understand growing of foods
* [SimpleLiquids](https://github.com/Terasology/SimpleLiquids) - lets water propagate in the world - beware of floods!
* [Soils](https://github.com/Terasology/Soils) - a small pack of different soil types
* [Spawning](https://github.com/Terasology/Spawning) - split out from Portals to serve as general utility for anything needing stuff to spawn
* [StructuralResources](https://github.com/Terasology/StructuralResources) - a set of structural shapes suitable for buildings and such
* [SubstanceMatters](https://github.com/Terasology/SubstanceMatters) - library for the definition and usage of materials in various contexts, such as tools with dynamic looks based on material
* [Tasks](https://github.com/Terasology/Tasks) - allows for the definition of tasks/quests
* [ThroughoutTheAges](https://github.com/Terasology/ThroughoutTheAges) - gameplay module for a large content series letting you slowly climb a tech tree to improve your available tools, foods, and so on
* [TutorialWorldGeneration](https://github.com/Terasology/TutorialWorldGeneration) - a world generation tutorial module, goes with a guide in its [wiki](https://github.com/Terasology/TutorialWorldGeneration/wiki)
* [Valentines](https://github.com/Terasology/Valentines) - What is love? Gooey don't hurt me, don't hurt me, no more ... ♫
* [WildAnimals](https://github.com/Terasology/WildAnimals) - a module containing animals, initially a deer you can spawn in-world via console with `spawnPrefab deer` then watch wander idly
* [WoodAndStone](https://github.com/Terasology/WoodAndStone) - big content module including "from scratch" crafting, starting with wood here
* [Workstation](https://github.com/Terasology/Workstation) - workstations offer a way to use blocks in-world for advanced purposes
* [WorkstationInGameHelp]((https://github.com/Terasology/WorkstationInGameHelp) - bridging module to bring in-game help to workstation screens
* [WorldlyTooltip](https://github.com/Terasology/WorldlyTooltip) - a little tooltip that shows you what you're looking at (hold `alt` for debug details)
* ~~[Zones](https://github.com/Terasology/Zones) - allows you to define zones within the world, that other modules can then use for assorted reasons~~ (broken, possibly deprecated)

Some of the modules in action:

![Terasology](/engine/src/main/resources/assets/textures/PopulatedVillage.jpg "Terasology")

Credits
--------

This is an incomplete list and the team is constantly growing.

Apologies in advance for any omissions, contact [Cervator](http://forum.terasology.org/members/cervator.2/) on the forum if you believe you've been missed :-)

Contributors
--------

(Listed by primary team)

* Architects: Benjamin 'begla' Glatzel, Immortius, Kai Kratz, Andre Herber, Panserbjoern, MarcinSc, Synopia, Xanhou, mkienenb, Gimpanse / shartte, Flo_K, emanuele3d
* Art Team: Glasz, A'nW, basilix, Double_A, eleazzaar, metouto, Perdemot, RampageMode, SuperSnark, Wolfghard, zproc, ChrisK, Maternal
* Design Team: Rasmus 'Cervator' Praestholm, Overdhose, Woodspeople, Mooncalf, Dei, UberWaffe, Chridal
* General: Janred, Josh, Stuthulhu, t3hk0d3, AbraCadaver, ahoehma, Brokenshakles, DizzyDragon / LinusVanElswijk, esereja, NowNewStart, pencilcheck, sdab, hagish, Philius342, temsa, nitrix, R41D3NN, Aperion, ilgarma, mcourteaux, philip-wernersbach, Xeano, Jamoozy, sdab, zriezenman, NanjoW, SleekoNiko, Eliwood, nh_99, jobernolte, emenifee, socram8888, dataupload, UltimateBudgie, maym86, aldoborrero, PrivateAlpha, CruzBishop, JoeClacks, Nate-Devv, Member1221, Jtsessions, porl, jacklin213, meniku, GeckoTheGeek42, IWhoI, Calinou, Limeth, KokPok, unpause, qreeves, Rui914, OvermindDL1, prestidigitator, chessandgo, gtugablue, sk0ut
* GUI Team: Anton "small-jeeper" Kireev, miniME89, x3ro, Halamix2
* Logistics Team: AlbireoX, Mathias Kalb, Richard "rapodaca" Apodaca, Stellarfirefly, mkalb, MrBarsack, Philaxx, 3000Lane, MiJyn, neoascetic
* World Team: bi0hax, ddr2, Nym Traveel, Skaldarnar, Tenson, Laurimann, MPratt, msteiger, Josharias

Soundtrack and Sound Effects
--------

* Primary soundtrack by Chris Köbke - https://soundcloud.com/chriskoebke
* Sunrise, Afternoon and Sunset composed by Karina Kireev.
* Dimlight, Resurface and Other Side composed and produced by Exile.
* Door Open sound by Pagancow, from FreeSound.org
* Door Close sound by Fresco, from FreeSound.org
* Camera Click Noise from Snapper4298, from FreeSound.org
* Other sound effects created by Exile.

Additional Notes
--------

Terasology's base graphics use the awesome <strong><a href="http://www.carrotcakestudios.co.uk/gmcraft/">Good Morning Craft!</a></strong> texture pack by Louis Durrant. Make sure to visit his <a href="http://www.carrotcakestudios.co.uk/">homepage</a> and support his work.

Our default font is ["Noto" by Google](http://www.google.com/get/noto), which is released under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html) just like Terasology.
