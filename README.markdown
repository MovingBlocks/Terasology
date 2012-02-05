Terasology (previously titled Blockmania)
=========================================

![Terasology](http://blog.movingblocks.net/wp-content/uploads/Blockmania030911-1.png "Terasology")

Terasology is an open source project started by Benjamin "begla" Glatzel to research *procedural terrain generation* and efficient rendering techniques in Java using the [LWJGL](http://lwjgl.org). The engine uses a *block-based voxel-like approach as seen in Minecraft*.

After proving itself as a solid *tech demo* begla was joined at first by Anton "small-jeeper" Kireev and Rasmus "Cervator" Praestholm and a full-fledged game concept was born.

Our goal is a game that pays ample tribute to [Minecraft](http://www.minecraft.net) in initial look and origin, but stakes out its own niche by adopting the NPC-helper and caretaker feel from such games as [Dwarf Fortress](http://www.bay12games.com/dwarves) and [Dungeon Keeper](http://en.wikipedia.org/wiki/Dungeon_Keeper), while striving for added depth and sophistication in the foundation systems akin to DF.

For more information on the project visit our blog: [Moving Blocks!](http://blog.movingblocks.net), check out our [Facebook Page](http://www.facebook.com/pages/Blockmania/248329655219905), or follow us on [Twitter](http://twitter.com/#!/Blockmania)!

If you're interested in joining up as a contributor register for our [forum](http://board.movingblocks.net) - the project is using the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

You can also fork the project on [GitHub](https://github.com/begla/Terasology)

Setup
-----

The game requires the latest version of Oracle's [Java Virtual Machine (JVM)](http://www.java.com/de/download/). Also make sure that your graphics card driver is up to date.

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

Credits
=======

Core Team
---------

* Benjamin "begla" Glatzel
* Anton "small-jeeper" Kireev
* Rasmus "Cervator" Praestholm

Soundtrack
----------

Composed by Karina Kireev.

Additional Notes
================

Terasology's base graphics use the awesome <strong><a href="http://www.carrotcakestudios.co.uk/gmcraft/">Good Morning Craft!</a></strong> texture pack by Louis Durrant. Make sure to visit his <a href="http://www.carrotcakestudios.co.uk/">homepage</a> and support his work.
