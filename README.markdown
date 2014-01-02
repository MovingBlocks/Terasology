Terasology
==========

![Terasology](/engine/src/main/resources/assets/textures/menuBackground.jpg "Terasology")

Terasology is a game that pays ample tribute to [Minecraft](http://www.minecraft.net) in initial look and origin, but stakes out its own niche by adopting the NPC-helper and caretaker feel from such games as [Dwarf Fortress](http://www.bay12games.com/dwarves) and [Dungeon Keeper](http://en.wikipedia.org/wiki/Dungeon_Keeper), while striving for added depth and sophistication.

Terasology is an open source project started by Benjamin "begla" Glatzel to research procedural terrain generation and efficient rendering techniques in Java using the [LWJGL](http://lwjgl.org). The engine uses a block-based voxel-like approach as seen in Minecraft.

The creators of Terasology are a diverse mix of software developers, game testers, graphic artists, and musicians. Get involved by checking out our [Community Portal](http://forum.movingblocks.net/index.php), our blog [Moving Blocks!](http://blog.movingblocks.net), and our [Facebook Page](http://www.facebook.com/pages/Terasology/248329655219905).

Terasology is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html) and available in source code form at [GitHub](https://github.com/MovingBlocks/Terasology).

Setup
--------

Terasology requires Java 7, the newer the better - [Java Virtual Machine (JVM)](http://www.java.com/en/download/manual.jsp). Also make sure that your graphics card driver is up to date.

For easy setup you can use our launcher - [download it here](https://github.com/MovingBlocks/TerasologyLauncher/releases)

For direct downloads you can get the latest [stable version here](http://jenkins.movingblocks.net/job/TerasologyStable/lastSuccessfulBuild/artifact/build/distributions/Terasology.zip) or our cutting-edge develop version [here from our Jenkins](http://jenkins.movingblocks.net/job/Terasology/lastSuccessfulBuild/artifact/build/distributions/Terasology.zip)

You can use the Windows executable or one of the default launch scripts to start the game. They will setup your Java Virtual Machine to allocate up to 1024 MB of memory. Under Linux and Mac OS X the run script needs the access permission "Execute" to run properly: "chmod +x [scriptname].sh".

Controls
--------

Note that these instructions are meant for the stable release. The latest develop build may differ and for more detailed instructions accurate to that release see our [GitHub Wiki](https://github.com/MovingBlocks/Terasology/wiki)

* [W,A,S,D] - Movement
* [E] - Activate (Chest, TNT, etc)
* [Q] - Throw held (block) item (hold down to charge for a longer throw!) OR start crafting (if placing object on ground with crafting module active)
* [Space] - Jump
* [Shift] - Hold to run
* [Left click] - Activate left click action (default = remove block)
* [Right click] - Activate right click action (default = place block)
* [Mouse wheel up/down] - Cycle through toolbar slots
* [1..0] - Change the active toolbar slot
* [I] - Toggle inventory screen
* [H] - Hide user interface
* [F] - Toggle viewing distance (near, moderate, far, ultra)
* [Tab] - Toggle developer console
* [Escape] - Show/hide the game menu screen
* [F1] - Toggle window focus
* [F3] - Toggle debug mode and information
* [F4] - Different debug metrics
* ~~[F5] - Show block picker GUI element~~

Debug Features
--------

Only works when the F3 debug mode is enabled (and may come and go)

* [Arrow up/down] - Adjust the current time in small steps
* [Arrow left/right] - Adjust the current time in larger steps
* [R] - Debug render wire-frame
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
* Sapling - an organically growing tree (grows one or a few blocks at a time)

Console Commands
--------

Press Tab to toggle the in-game console. Block names and some other things are not capital sensitive while command names are. Copy paste is supported and up/down arrow will cycle through commands you've used before

* ghost - fly / no-clip mode (old double-jump for "god" mode)
* hspeed - radically increase your horizontal move speed
* hjump - jump really high. Almost like flying - but it isn't. Just jump good.
* restoreSpeed - normalizes speed (both horizontal and vertical)
* help - Show in-game help (more thorough)
* help "giveBlock" - Shows detailed help on the "giveBlock" command
* giveBlock "Water" - Gives 16 water blocks
* giveBlock "Stone" "Slope" - Gives you 16 clay blocks in the "slope" shape
* giveBlock "Marble" "Stair" 99 - Gives you 99 marble stair blocks
* giveBlock "Chest" - Gives you a Chest block you can place, activate ('E'), put stuff in, break, pick up, place elsewhere, find same stuff in it!
* giveBlock "TNT" - Gives you 16 TNT blocks you can place and activate ('E') to blow up
* listBlocks - Lists all actively used blocks (have been loaded for the world)
* listFreeShapeBlocks - Lists all blocks that can be requested in any known shape
* listShapes - Lists the available shapes
* ~~teleport 42 42 42 - Warps the player to x = 42, y = 42, z = 42~~
* ~~fullHealth - Fully restores the player's health~~

Building and running source
--------

Run any commands in the project root directory

*  Download / clone the source from GitHub
*  To prepare for IntelliJ run: `gradlew idea`
*  To prepare for Eclipse run: `gradlew eclipse`
*  To run from the command line: `gradlew run`
*  For more tasks: `gradlew tasks`

You may also need to tweak IDE settings further for your convenience. See [Dev Setup](https://github.com/MovingBlocks/Terasology/wiki/Dev-Setup) in our wiki for more details.

Note that by default stored data (config, saves, etc) is sent to a user profile directory like Saved Games under Windows, even when running from source. Add `-homedir` to a run configuration to use the project dir instead.

Modules
--------

Content, gameplay mechanics, and mostly everything other than the engine that allows the game to run is stored in what we call "modules" which are _similar_ to the traditional meaning of "mods" but intended to be smaller building blocks you'd normally put several of together to make one true "mod".

Modules must be enabled during world creation by selecting them using the "Modules" button. Some world generator types may be registered by a module and auto-select that and maybe other modules if you choose that world type. Modules may also enable additional console commands (listed by the "help" command when active).

Here's a list of modules bundled with the game by default (as of this writing anyway - this line-up will change now and then). It should roughly match this category in Jenkins: http://jenkins.movingblocks.net/view/Modules and you can download updated modules from there if needed.

* [BlockNetwork](https://github.com/Terasology/BlockNetwork) - a framework to support blocks that can communicate with each other in some fashion
* [Cities](https://github.com/Terasology/Cities) - procedural city placer and plot organizer, also places roads to connect cities
* Core - mandatory content needed for normal game launch
* [Crops](https://github.com/Terasology/Crops) - a series of crop-like plants with multiple growth stages
* [Fences](https://github.com/Terasology/Fences) - fences!
* [FunnyBlocks](https://github.com/Terasology/FunnyBlocks) - cheese wheels and bowling pins - why not
* [Hunger](https://github.com/Terasology/Hunger) - makes the player slowly gets hungry (needs actual GUI work and ways to then actually eat food though)
* [LightAndShadowResources](https://github.com/Terasology/LightAndShadowResources) - IMMA FIRIN’ MAH LASR!! Art assets for the Light & Shadow concept
* [Malicious](https://github.com/Terasology/Malicious) - a series of module security tests to check that modules cannot do naughty things when running
* [Maze](https://github.com/Terasology/Maze) - a maze generator. Right-click with the provided maze tool on one block then again on another and a maze will generate between
* [Minerals](https://github.com/Terasology/Minerals) - a large collection of mineral blocks
* [MoreLights](https://github.com/Terasology/MoreLights) - assorted illuminated blocks
* [NameGenerator](https://github.com/Terasology/NameGenerator) - can create random themed names for use by other modules, or via console using commands like 'generateNameList 10'
* [Sample](https://github.com/Terasology/Sample) - miscellaneous example content showcasing module usage
* [Signalling](https://github.com/Terasology/Signalling) - circuitry implementation based on BlockNetwork, similar to redstone
* [Soils](https://github.com/Terasology/Soils) - a small pack of different soil types

Some of the modules in action:

![Terasology](/engine/src/main/resources/assets/textures/PopulatedVillage.jpg "Terasology")

Credits
--------

This is an incomplete list and the team is constantly growing. See also [Dev Team](https://github.com/MovingBlocks/Terasology/wiki/Dev-team) in the wiki but at least one of them is bound to be out of date

Apologies in advance for any omissions, contact [Cervator](http://forum.movingblocks.net/members/cervator.2/) on the forum if you believe you've been missed :-)

Contributors
--------

(Listed by primary team)

* Architects: Benjamin 'begla' Glatzel, Immortius, Kai Kratz, Andre Herber, Panserbjoern, MarcinSc, Synopia, Xanhou
* Art Team: Glasz, A'nW, basilix, Double_A, eleazzaar, metouto, Perdemot, RampageMode, SuperSnark, Wolfghard, zproc, Chrisk, Maternal
* Design Team: Rasmus 'Cervator' Praestholm, Overdhose, Woodspeople, Mooncalf, Dei, UberWaffe, Chridal
* General: Janred, Josh, Stuthulhu, t3hk0d3, AbraCadaver, ahoehma, Brokenshakles, DizzyDragon, esereja, MiJyn, NowNewStart, pencilcheck, sdab, hagish, Philius342, temsa, nitrix, R41D3NN, Aperion, ilgarma, mcourteaux, 3000Lane, philip-wernersbach, Xeano, Jamoozy, sdab, zriezenman, NanjoW, SleekoNiko, Eliwood, nh_99, jobernolte, emenifee, socram8888, dataupload, mkienenb, UltimateBudgie, maym86, aldoborrero, PrivateAlpha, Josharias
* GUI Team: Anton "small-jeeper" Kireev, miniME89, x3ro
* Logistics Team: AlbireoX, Mathias Kalb, Richard "rapodaca" Apodaca, Stellarfirefly, mkalb, MrBarsack, Philaxx
* World Team: bi0hax, ddr2, Nym Traveel, Skaldarnar, Tenson, Laurimann, MPratt, msteiger

Soundtrack and Sound Effects
--------

* Primary soundtrack by ChrisK - https://soundcloud.com/chriskoebke
* Sunrise, Afternoon and Sunset composed by Karina Kireev.
* Dimlight, Resurface and Other Side composed and produced by Exile.
* Door Open sound by Pagancow, from FreeSound.org
* Door Close sound by Fresco, from FreeSound.org
* Other sound effects created by Exile.

Additional Notes
--------

Terasology's base graphics use the awesome <strong><a href="http://www.carrotcakestudios.co.uk/gmcraft/">Good Morning Craft!</a></strong> texture pack by Louis Durrant. Make sure to visit his <a href="http://www.carrotcakestudios.co.uk/">homepage</a> and support his work.
