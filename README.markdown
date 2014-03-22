Terasology
==========

![Terasology](/engine/src/main/resources/assets/textures/menuBackground.jpg "Terasology")

Terasology is a game that pays ample tribute to [Minecraft](http://www.minecraft.net) in initial look and origin, but stakes out its own niche by aiming for the NPC-helper and caretaker focus from such games as [Dwarf Fortress](http://www.bay12games.com/dwarves) and [Dungeon Keeper](http://en.wikipedia.org/wiki/Dungeon_Keeper), while striving for added depth and sophistication.

Terasology is an open source project started by Benjamin "begla" Glatzel to research procedural terrain generation and efficient rendering techniques in Java using the [LWJGL](http://lwjgl.org). The engine uses a block-based voxel-like approach as seen in Minecraft.

The creators of Terasology are a diverse mix of software developers, game testers, graphic artists, and musicians. Get involved by checking out our [Community Portal](http://forum.movingblocks.net/index.php), our blog [Moving Blocks!](http://blog.movingblocks.net), and our [Facebook Page](http://www.facebook.com/pages/Terasology/248329655219905).

Terasology is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html) and available in source code form at [GitHub](https://github.com/MovingBlocks/Terasology).

Setup
--------

Terasology requires Java 7, the newer the better - [download it here](http://www.java.com/en/download/manual.jsp). Also make sure that your graphics card driver is up to date.

For easy setup you can use our launcher - [download it here](https://github.com/MovingBlocks/TerasologyLauncher/releases)

For direct downloads you can get the latest [stable version here](http://jenkins.movingblocks.net/job/TerasologyStable/lastSuccessfulBuild/artifact/build/distributions/Terasology.zip) or our cutting-edge develop version [here from our Jenkins](http://jenkins.movingblocks.net/job/Terasology/lastSuccessfulBuild/artifact/build/distributions/Terasology.zip)

You can use the Windows executable or one of the default launch scripts to start the game. They will setup your Java Virtual Machine to allocate up to 1024 MB of memory. Under Linux and Mac OS X the run script needs the access permission "Execute" to run properly: "chmod +x [scriptname].sh".

Controls
--------

Note that these instructions are meant for the stable release. The latest develop build may differ and for more detailed instructions accurate to that release see our [GitHub Wiki](https://github.com/MovingBlocks/Terasology/wiki)

* [W,A,S,D] - Movement
* [E] - Activate (Chest, TNT, etc)
* [Q] - Throw held (block) item (hold down to charge for a longer throw!)
* [Space] - Jump
* [Shift] - Hold to run
* [Left click] - Activate left click action (default = remove block)
* [Right click] - Activate right click action (default = place block)
* [Mouse wheel up/down] - Cycle through toolbar slots OR pick up / deposit items into stacks in an inventory one at a time
* [1..0] - Change the active toolbar slot
* [I] - Toggle inventory screen
* [H] - Hide user interface
* [HOME] - Toggle viewing distance
* [`] - Toggle developer console (the "grave" key, above tab)
* [Tab] - Auto-completion in the console
* [Escape] - Show/hide the game menu screen
* [F1] - Toggle window focus
* [F3] - Toggle debug mode and information
* [F5] - Show behavior tree editor
* ~~[F5] - Show block picker GUI element~~

Debug Features
--------

Only works when the F3 debug mode is enabled (and may come and go)

* [Arrow up/down] - Adjust the current time in small steps
* [Arrow left/right] - Adjust the current time in larger steps
* [R] - Debug render wire-frame
* [F4] - Cycle advanced debug metrics
* [F6] - Debug rendering enabled
* [F7] - Cycle debug rendering stage
* [F8] - Debug render chunk bounding boxes

Tools
--------

May move slot or disappear as development continues

* Axe / Pickaxe - Faster right-click removal of some blocks
* Torch - Shiny! Place with left click. Try throwing these in water at night
* Explosion tool - Big bada boom!
* Railgun  - Bigger bada boom, in a straight line!
* ~~Minituarizer (scissors) - Left click one block, then another, then somewhere else to "clone" your selection in a tiny accurate representation~~
* Goodie chest - place it and open with 'e' for assorted goodies

More or completely alternative line-ups with certain modules / world types selected

Console Commands
--------

Press the `grave` key (usually the ony immediately above `tab`) to show the in-game console. Block names and some other things are not capital sensitive while command names are. Copy paste is supported and up/down arrow will cycle through commands you've used before. Hitting `tab` with a partially typed command will auto-complete it (including abbreviated camel case like lS for listShapes)

* help - Show in-game help (more thorough)
* ghost - fly / no-clip mode (old double-jump for "god" mode)
* hspeed - radically increase your horizontal move speed
* hjump - jump really high. Almost like flying - but it isn't. Just jump good.
* restoreSpeed - normalizes speed (both horizontal and vertical)
* help "giveBlock" - Shows detailed help on the "giveBlock" command
* giveBlock "Water" - Gives 16 water blocks
* giveBlock "Stone" "Stair" 99 - Gives you 99 stone stair blocks
* giveBlock "Chest" - Gives you a Chest block you can place, activate ('E'), put stuff in, break, pick up, place elsewhere, find same stuff in it!
* giveBlock "TNT" - Gives you 16 TNT blocks you can place and activate ('E') to blow up
* listBlocks - Lists all actively used blocks (have been loaded for the world)
* listFreeShapeBlocks - Lists all blocks that can be requested in any known shape
* listShapes - Lists the available shapes
* ~~teleport 42 42 42 - Warps the player to x = 42, y = 42, z = 42~~
* ~~fullHealth - Fully restores the player's health~~

Building and running from source
--------

Run any commands in the project root directory

*  Download / clone the source from GitHub
*  To prepare for IntelliJ run: `gradlew idea`
*  To prepare for Eclipse run: `gradlew eclipse`
*  To run from the command line: `gradlew run`
*  For more tasks: `gradlew tasks`

You may also need to tweak IDE settings further for your convenience, in particular for Eclipse. See [Dev Setup](https://github.com/MovingBlocks/Terasology/wiki/Dev-Setup) in our wiki for more details.

Note that by default stored data (config, saves, etc) is sent to a user profile directory like Saved Games under Windows. Add `-homedir` to a run configuration or command line execution to use the project dir instead (this is done automatically in some cases including IntelliJ and `gradlew run`)

If you want to pull down the source code for a module you can easily do so via Gradle:

* `gradlew fetchModuleSample`
* `gradlew idea`

This fetches the module source for the "Sample" module and the second command fetches any dependencies and updates the IntelliJ project structure so you can see it as a module. Likewise for Eclipse or any other setup you should run any one `gradlew` command to make sure the new module's dependencies have been fetched as well. See [Codebase Structure](https://github.com/MovingBlocks/Terasology/wiki/Codebase-Structure) in the wiki for more.

Modules
--------

Content, gameplay mechanics, and mostly everything other than the engine that allows the game to run is stored in what we call "modules" which are _similar_ to the traditional meaning of "mods" but intended to be smaller building blocks you'd normally put several of together to make one true "mod".

Modules must be enabled during world creation by selecting them using the "Modules" button. Some world generator types may be registered by a module and auto-select that and maybe other modules if you choose that world type. Modules may also enable additional console commands (listed by the "help" command when active).

Here's a list of modules bundled with the game by default (as of this writing anyway - this line-up will change now and then). It should roughly match this category in Jenkins: http://jenkins.movingblocks.net/view/Modules and you can download updated modules from there if needed.

* [AlterationEffects](https://github.com/Terasology/AlterationEffects) - module for storing some buff/debuff type effects
* [AnotherWorld](https://github.com/Terasology/AnotherWorld) - world gen module, includes features like ore placement and caves, used by WoodAndStone's world
* [BlockNetwork](https://github.com/Terasology/BlockNetwork) - a framework to support blocks that can communicate with each other in some fashion
* [CakeLie](https://github.com/Terasology/CakeLie) - the cake may be a lie, but these cake and candy blocks are delicious!
* [ChangingBlocks](https://github.com/Terasology/ChangingBlocks) - allows blocks that change over time (like crops that grow - which will happen if you also enable Crops)
* [Cities](https://github.com/Terasology/Cities) - procedural city placer and plot organizer, also places roads to connect cities
* [CopperAndBronze](https://github.com/Terasology/CopperAndBronze) - another era for throughout the ages, this time copper and bronze (comes after wood and stone)
* Core - mandatory content needed for normal game launch
* [Crops](https://github.com/Terasology/Crops) - a series of crop-like plants with multiple growth stages
* [Fences](https://github.com/Terasology/Fences) - fences!
* [Fluid](https://github.com/Terasology/Fluid) - adds support for fluid in non-world situations (such as for storage in workstations)
* [FunnyBlocks](https://github.com/Terasology/FunnyBlocks) - cheese wheels and bowling pins - why not
* [Genome](https://github.com/Terasology/Genome) - genetics WOO! Complete with DNA letters and mutating plants. Part of the Wood & Stone line-up
* [GrowingFlora](https://github.com/Terasology/GrowingFlora) - organically growing (step by step) trees and such
* [Hunger](https://github.com/Terasology/Hunger) - makes the player slowly gets hungry (needs actual GUI work and ways to then actually eat food though). Console `hungerCheck` for stats
* [Journal](https://github.com/Terasology/Journal) - allows the player to use an in-game journal for gameplay notifications and such
* [LightAndShadow](https://github.com/Terasology/LightAndShadow) - main module for the Light & Shadow gameplay
* [LightAndShadowResources](https://github.com/Terasology/LightAndShadowResources) - IMMA FIRIN’ MAH LASR!! Art assets for the Light & Shadow concept
* [Malicious](https://github.com/Terasology/Malicious) - a series of module security tests to check that modules cannot do naughty things when running
* [Maze](https://github.com/Terasology/Maze) - a maze generator. Right-click with the provided maze tool on one block then again on another and a maze will generate between the two points (in multiple layers if the area is tall enough)
* [Minerals](https://github.com/Terasology/Minerals) - a large collection of mineral blocks
* ~~[Miniion](https://github.com/Terasology/Miniion) - old school miniions are back! Mostly working (edit: not so much anymore, alas!) :D~~
* [Minimap](https://github.com/Terasology/Minimap) - a basic minimap using "slicing" (showing a single layer at a time as per a selected axis)
* [MoreLights](https://github.com/Terasology/MoreLights) - assorted illuminated blocks
* [MultiBlock](https://github.com/Terasology/MultiBlock) - supports the concept of multiple blocks being part of the same structure
* [NameGenerator](https://github.com/Terasology/NameGenerator) - can create random themed names for use by other modules, or via console using commands like `generateNameList 10`
* [Oreons](https://github.com/Terasology/Oreons) - little sentient cookie people! Don't do much yet. Place with `spawnPrefab "Oreons:OreonGuard"` in the console
* [Pathfinding](https://github.com/Terasology/Pathfinding) - framework for pathfinding used by other modules
* [PlantPack](https://github.com/Terasology/PlantPack) - more plants! Used by the Wood and Stone gameplay
* [Portals](https://github.com/Terasology/Portals) - allows placement of portal blocks that'll spawn Oreons `giveBlock "portal"`
* [Sample](https://github.com/Terasology/Sample) - miscellaneous example content showcasing module usage
* ~~[Signalling](https://github.com/Terasology/Signalling) - circuitry implementation based on BlockNetwork, similar to redstone~~
* [Soils](https://github.com/Terasology/Soils) - a small pack of different soil types
* [Spawning](https://github.com/Terasology/Spawning) - split out from Portals to serve as general utility for anything needing stuff to spawn
* [WoodAndStone](https://github.com/Terasology/WoodAndStone) - big gameplay module featuring "from scratch" crafting throughout the ages - wood here
* [Workstation](https://github.com/Terasology/Workstation) - workstations offer a way to use blocks in-world for advanced purposes
* ~~[Zones](https://github.com/Terasology/Zones) - allows you to define zones within the world, that other modules can then use for assorted reasons~~

Some of the modules in action:

![Terasology](/engine/src/main/resources/assets/textures/PopulatedVillage.jpg "Terasology")

Credits
--------

This is an incomplete list and the team is constantly growing. See also [Dev Team](https://github.com/MovingBlocks/Terasology/wiki/Dev-team) in the wiki but at least one of them is bound to be out of date

Apologies in advance for any omissions, contact [Cervator](http://forum.movingblocks.net/members/cervator.2/) on the forum if you believe you've been missed :-)

Contributors
--------

(Listed by primary team)

* Architects: Benjamin 'begla' Glatzel, Immortius, Kai Kratz, Andre Herber, Panserbjoern, MarcinSc, Synopia, Xanhou, mkienenb
* Art Team: Glasz, A'nW, basilix, Double_A, eleazzaar, metouto, Perdemot, RampageMode, SuperSnark, Wolfghard, zproc, Chrisk, Maternal
* Design Team: Rasmus 'Cervator' Praestholm, Overdhose, Woodspeople, Mooncalf, Dei, UberWaffe, Chridal
* General: Janred, Josh, Stuthulhu, t3hk0d3, AbraCadaver, ahoehma, Brokenshakles, DizzyDragon, esereja, NowNewStart, pencilcheck, sdab, hagish, Philius342, temsa, nitrix, R41D3NN, Aperion, ilgarma, mcourteaux, philip-wernersbach, Xeano, Jamoozy, sdab, zriezenman, NanjoW, SleekoNiko, Eliwood, nh_99, jobernolte, emenifee, socram8888, dataupload, UltimateBudgie, maym86, aldoborrero, PrivateAlpha, CruzBishop, JoeClacks, Nate-Devv
* GUI Team: Anton "small-jeeper" Kireev, miniME89, x3ro
* Logistics Team: AlbireoX, Mathias Kalb, Richard "rapodaca" Apodaca, Stellarfirefly, mkalb, MrBarsack, Philaxx, 3000Lane, MiJyn, neoascetic
* World Team: bi0hax, ddr2, Nym Traveel, Skaldarnar, Tenson, Laurimann, MPratt, msteiger, Josharias

Soundtrack and Sound Effects
--------

* Primary soundtrack by ChrisK - https://soundcloud.com/chriskoebke
* Sunrise, Afternoon and Sunset composed by Karina Kireev.
* Dimlight, Resurface and Other Side composed and produced by Exile.
* Door Open sound by Pagancow, from FreeSound.org
* Door Close sound by Fresco, from FreeSound.org
* Camera Click Noise from Snapper4298, from FreeSound.org
* Other sound effects created by Exile.

Additional Notes
--------

Terasology's base graphics use the awesome <strong><a href="http://www.carrotcakestudios.co.uk/gmcraft/">Good Morning Craft!</a></strong> texture pack by Louis Durrant. Make sure to visit his <a href="http://www.carrotcakestudios.co.uk/">homepage</a> and support his work.
