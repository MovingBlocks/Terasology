Terasology (previously titled Blockmania)
=========================================

![Terasology](http://blog.movingblocks.net/wp-content/uploads/screen3small.jpg "Terasology")

Terasology is a game that pays ample tribute to [Minecraft](http://www.minecraft.net) in initial look and origin, but stakes out its own niche by adopting the NPC-helper and caretaker feel from such games as [Dwarf Fortress](http://www.bay12games.com/dwarves) and [Dungeon Keeper](http://en.wikipedia.org/wiki/Dungeon_Keeper), while striving for added depth and sophistication.

Terasology is an open source project started by Benjamin "begla" Glatzel to research procedural terrain generation and efficient rendering techniques in Java using the [LWJGL](http://lwjgl.org). The engine uses a block-based voxel-like approach as seen in Minecraft. After proving itself as a solid tech demo begla was joined at first by Anton "small-jeeper" Kireev and Rasmus "Cervator" Praestholm and a full-fledged game concept was born.

The creators of Terasology are a diverse mix of software developers, game testers, graphic artists, and musicians. Get involved by checking out the [Forum](http://board.movingblocks.net/index.php), our blog [Moving Blocks!](http://blog.movingblocks.net), and our [Facebook Page](http://www.facebook.com/pages/Blockmania/248329655219905).

Terasology is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html) and available in source code form at [GitHub](https://github.com/MovingBlocks/Terasology).

Setup
-----

Terasology requires the latest version of Oracle's [Java Virtual Machine (JVM)](http://www.java.com/de/download/). Also make sure that your graphics card driver is up to date.

You can use one of the default launch scripts to start the game. The scripts will setup your JVM to allocate up to 1024 MB of heap space. Under Linux and Mac OS X the run script needs the access permission "Execute" to run properly: "chmod +x script.sh".

Controls
--------

* [W,A,S,D]               Walking
* [Space]                 Jump
* [Shift]                 Hold for running
* [Left click]            Activate left click action (default = place block)
* [Right click]           Activate right click action (default = remove block)
* [Mouse wheel up/down]   Cycle through toolbar slots
* [1,..,0]                Change the active toolbar slot
* [I]                     Toggle inventory screen
* [F]                     Toggle viewing distance (near, moderate, far, ultra)
* [Tab]                   Toggle developer console
* [F3]                    Toggle debug mode and information
* [F4]                    Different debug metrics
* [2*Space]               God mode
* [K]                     Don't try this :-)
* [Escape]                Show/hide the game menu screen

Debug features (only works when debug mode is enabled
------------------------

* [Arrow up/down]         Adjust the current time in small steps

Examples tools (may move slot or disappear)
------------------------

* Torch - shiny! Place with left click
* Pickaxe / shovel - faster right-click removal of some blocks
* Blueprint - left click one block, then another, then somewhere else to "clone" your selection (right-click resets)
* Lighter - big bada boom!

Example console commands
------------------------

* "tera.initWorld();"                               Init. a new random world
* "tera.getActiveWorldProvider().setTime(0.0);"     Set the world time

Building and running source
------------------------

1.  Install Gradle from http://gradle.org/
2.  To import into Eclipse: $ gradle eclipse
3.  To import into IntelliJ: $ gradle idea
4.  To run from the command line: $ gradle run
5.  For more tasks: $ gradle tasks

You may also need to tweak IDE settings further for your convenience. See [Dev Setup](http://wiki.movingblocks.net/bin/view/Main/DevSetup) in our wiki for more details.

Credits
=======

This is an incomplete list and the team is constantly growing. See [Dev Team](http://wiki.movingblocks.net/bin/view/Main/DevTeam) in the wiki for the latest updates

Contributors
---------

* Benjamin "begla" Glatzel
* Anton "small-jeeper" Kireev
* Rasmus "Cervator" Praestholm
* Immortius
* Richard Apodaca (rapodaca)
* Kai Kratz
* t3hk0d3

Soundtrack and Sound Effects
----------

* Sunrise, Afternoon and Sunset composed by Karina Kireev.
* Dimlight, Resurface and Other Side composed and produced by Exile.
* Sound effects created by Exile.

Additional Notes
================

Terasology's base graphics use the awesome <strong><a href="http://www.carrotcakestudios.co.uk/gmcraft/">Good Morning Craft!</a></strong> texture pack by Louis Durrant. Make sure to visit his <a href="http://www.carrotcakestudios.co.uk/">homepage</a> and support his work.
