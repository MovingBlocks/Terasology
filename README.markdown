Terasology
==========

![Terasology](/src/main/resources/assets/textures/menuBackground.png "Terasology")

Terasology is a game that pays ample tribute to [Minecraft](http://www.minecraft.net) in initial look and origin, but stakes out its own niche by adopting the NPC-helper and caretaker feel from such games as [Dwarf Fortress](http://www.bay12games.com/dwarves) and [Dungeon Keeper](http://en.wikipedia.org/wiki/Dungeon_Keeper), while striving for added depth and sophistication.

Terasology is an open source project started by Benjamin "begla" Glatzel to research procedural terrain generation and efficient rendering techniques in Java using the [LWJGL](http://lwjgl.org). The engine uses a block-based voxel-like approach as seen in Minecraft. After proving itself as a solid tech demo begla was joined at first by Anton "small-jeeper" Kireev and Rasmus "Cervator" Praestholm and a full-fledged game concept was born.

The creators of Terasology are a diverse mix of software developers, game testers, graphic artists, and musicians. Get involved by checking out our [Community Portal](http://forum.movingblocks.net/index.php), our blog [Moving Blocks!](http://blog.movingblocks.net), and our [Facebook Page](http://www.facebook.com/pages/Terasology/248329655219905).

Terasology is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html) and available in source code form at [GitHub](https://github.com/MovingBlocks/Terasology).

Setup
--------

Terasology requires the latest version of Oracle's [Java Virtual Machine (JVM)](http://www.java.com/en/download/manual.jsp). Also make sure that your graphics card driver is up to date.

Download the latest [stable version here](http://jenkins.movingblocks.net/job/TerasologyStable/lastSuccessfulBuild/artifact/build/distributions/Terasology.zip) or our cutting-edge develop version [here from our Jenkins](http://jenkins.movingblocks.net/job/Terasology/lastSuccessfulBuild/artifact/build/distributions/Terasology.zip)

You can use one of the default launch scripts to start the game. The scripts will setup your JVM to allocate up to 1024 MB of heap space. Under Linux and Mac OS X the run script needs the access permission "Execute" to run properly: "chmod +x script.sh".

Controls
--------

Note that these instructions are meant for the stable release. The latest develop build may differ and for more detailed instructions accurate to that release see our [GitHub Wiki](https://github.com/MovingBlocks/Terasology/wiki)

* [W,A,S,D] - Movement
* [E] - Activate (Chest, TNT, etc)
* [Q] - Throw held (block) item (hold down to charge for a longer throw!) OR start crafting (if placing object on ground with crafting module active)
* [Space] - Jump
* [Double Space] - God mode (fly / no-clip)
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
* [F3] - Toggle debug mode and information
* [F4] - Different debug metrics
* [F5] - Show block picker GUI element

Debug Features
--------

Only works when the F3 debug mode is enabled (and may come and go)

* [Arrow up/down] - Adjust the current time in small steps
* [P] - Activate first-person player camera
* [O] - Activate animated spawning point camera
* [K] - Don't try this :-)

Tools
--------

May move slot or disappear as development continues

* Axe / Pickaxe - Faster right-click removal of some blocks
* Torch - Shiny! Place with left click
* Minituarizer (scissors) - Left click one block, then another, then somewhere else to "clone" your selection in a tiny accurate representation
* Explosion tool - Big bada boom!
* Railgun  - Bigger bada boom, in a straight line!
* Goodie chest - place it and open with 'e' for assorted goodies

Console Commands
--------

Press Tab to toggle the in-game console. Block names and some other things are not capital sensitive. Copy paste is supported and up/down arrow will cycle through commands you've used before

* /help - Show in-game help (more thorough)
* /help "giveBlock" - Shows detailed help on the "giveBlock" command
* /giveBlock "Water" - Gives 16 water blocks
* /giveBlock "Rutile" 42 - Gives 42 Rutile blocks (colorful mineral)
* /giveBlock "Clay" "Slope" - Gives you 16 clay blocks in the "slope" shape
* /giveBlock "Marble" "Stair" 99 - Gives you 99 marble stair blocks
* /giveBlock "Chest" - Gives you a Chest block you can place, activate ('E'), put stuff in, destroy, pick up, place elsewhere, find same stuff in it!
* /giveBlock "TNT" - Gives you 16 TNT blocks you can place and activate ('E') to blow up
* /listBlocks - Lists all actively used blocks (have been loaded for the world)
* /listFreeShapeBlocks - Lists all blocks that can be requested in any known shape
* /listShapes - Lists the available shapes
* /teleport 42 42 42 - Warps the player to x = 42, y = 42, z = 42
* /fullHealth - Fully restores the player's health

Building and running source
--------

Run any commands in the project root directory

*  Download / clone the source from GitHub
*  To prepare for IntelliJ run: `gradlew idea`
*  To prepare for Eclipse run: `gradlew eclipse`
*  To run from the command line: `gradlew run`
*  For more tasks: `gradlew tasks`

You may also need to tweak IDE settings further for your convenience. See [Dev Setup](https://github.com/MovingBlocks/Terasology/wiki/Dev-Setup) in our wiki for more details.

Credits
--------

This is an incomplete list and the team is constantly growing. See [Dev Team](https://github.com/MovingBlocks/Terasology/wiki/Dev-team) in the wiki for the latest updates

Apologies in advance for any omisions, contact [Cervator](http://forum.movingblocks.net/members/cervator.2/) on the forum if you believe you've been missed :-)

Contributors
--------

(Listed by primary team)

* Architects: Benjamin 'begla' Glatzel, Immortius, Kai Kratz, Ironchefpython, Andre Herber, Panserbjoern
* Art Team: Glasz, A'nW, basilix, Double_A, eleazzaar, metouto, Perdemot, RampageMode, SuperSnark, Wolfghard, zproc, Chrisk, Maternal
* Design Team: Rasmus 'Cervator' Praestholm, Overdhose, Woodspeople, Mooncalf, Dei
* General: Janred, Josh, Stuthulhu, t3hk0d3, AbraCadaver, ahoehma, Brokenshakles, DizzyDragon, esereja, MiJyn, NowNewStart, pencilcheck, sdab, hagish, Philius342, temsa, nitrix, R41D3NN, Aperion, ilgarma, mcourteaux, 3000Lane, philip-wernersbach
* GUI Team: Anton "small-jeeper" Kireev, miniME89, x3ro
* Logistics Team: AlbireoX, Mathias Kalb, Richard "rapodaca" Apodaca, Stellarfirefly, mkalb, MrBarsack, Philaxx
* World Team: B!0HAX, ddr2, Nym Traveel, Skaldarnar, Tenson, Laurimann


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
