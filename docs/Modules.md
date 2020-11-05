# Modules

Content, gameplay mechanics, and mostly everything other than the engine that allows the game to run is stored in what we call "modules" which are _similar_ to the traditional meaning of "mods" but intended to be smaller building blocks you'd normally put several of together to make one traditional "mod".

Modules in turn are sometimes grouped in "Gameplay Templates" which are like Minecraft-style mod packs. These are directly available via drop-down at world creation and are the suggested way to enable more content. Do **not** enable every single module manually! :-) 

You can customize module selection with the "Modules" button but you generally should enable very few or rely on the gameplay templates. Some world generator types may be registered by a module and auto-select that and maybe other modules if you choose that world type. Modules may also enable additional console commands (listed by the "help" command when active).

The game can download module updates from our central meta-server and clients connecting to a game server will be automatically sent the right version of any enabled modules! 

Be cautious in downloading modules beyond the ones shipped with the ["Omega Zip"](https://github.com/MovingBlocks/Terasology/releases) - other modules not bundled with the game are likely still in development or unstable and may crash the game.

Module source repositories live under the [Terasology organization](https://github.com/Terasology) on GitHub, while the engine level projects live under the [MovingBlocks organization](https://github.com/MovingBlocks).

Here's a list of modules bundled with the game by default (this line-up will change now and then). It should roughly match this category in Jenkins: http://jenkins.terasology.org/view/Modules and you can download updated modules from there if needed.

* [AdditionalFruits](https://github.com/Terasology/AdditionalFruits) - container module for more fruits
* [AdditionalItemPipes](https://github.com/Terasology/AdditionalItemPipes) - container module for more item pipes
* [AdditionalRails](https://github.com/Terasology/AdditionalRails) - container module for more rails
* [AdditionalVegetables](https://github.com/Terasology/AdditionalVegetables) - container module for more vegetables
* [AdvancedRails](https://github.com/Terasology/AdvancedRails) - more advanced content for rail systems
* [AdventureAssets](https://github.com/Terasology/AdventureAssets) - a set of assets for adventure time!
* [Alchemy](https://github.com/Terasology/Alchemy) - create alchemical potions!
* [AlchemyPlantGenerator](https://github.com/Terasology/AlchemyPlantGenerator) - a world plugin that'll seed the plants used by Alchemy into a given plugin-capable world
* [AlterationEffects](https://github.com/Terasology/AlterationEffects) - module for storing some buff/debuff type effects
* [Anatomy](https://github.com/Terasology/Anatomy) - an anatomical system meant to provide an alternative to typical HP only systems
* [AnotherWorld](https://github.com/Terasology/AnotherWorld) - world gen module, includes features like ore placement and caves, used by WoodAndStone's world
* [AnotherWorldPlants](https://github.com/Terasology/AnotherWorldPlants) - farming and tree growth for AnotherWorld
* [Apiculture](https://github.com/Terasology/Apiculture) - BEES! RUN! Well, eventually, they're still being trained to do stuff. GSOC 2019
* [BasicCrafting](https://github.com/Terasology/BasicCrafting) - a basic crafting system
* [Behaviors](https://github.com/Terasology/Behaviors) - library module containing various behavior trees for use in our AI system
* [BiomesAPI](https://github.com/Terasology/BiomesAPI) - library module for basic Biome functionality
* [BlockDetector](https://github.com/Terasology/BlockDetector) - adds a simple little tool that can detect specific blocks (beeping in proximity)
* [BlockNetwork](https://github.com/Terasology/BlockNetwork) - a framework to support blocks that can communicate with each other in some fashion
* [BlockPicker](https://github.com/Terasology/BlockPicker) - allows the player access to an infinite block inventory with the `B` key
* [Books](https://github.com/Terasology/Books) - books and bookcases
* [Breathing](https://github.com/Terasology/Breathing) - without this you can't breathe! But you also don't need to. Enable it to drown properly in water (or not!)
* [CakeLie](https://github.com/Terasology/CakeLie) - the cake may be a lie, but these cake and candy blocks are delicious!
* [Caves](https://github.com/Terasology/Caves) - a cave generation module (actually an ore vein generator placing air veins!) based on CustomOreGen
* [ChangingBlocks](https://github.com/Terasology/ChangingBlocks) - allows blocks to change over time (such as switching to a more dusty / worn looking block after x amount of days)
* [CheatsForAll](https://github.com/Terasology/CheatsForAll) - if enabled on a server allows the use of `cheat` commands for any player connected without any additional admin setup
* [ChiselBlocks](https://github.com/Terasology/ChiselBlocks) - assets for any branch of Chisel to use
* [ChrisVolume1OST](https://github.com/Terasology/ChrisVolume1OST) - official game soundtrack by Chris Köbke - volume 1
* [ChrisVolume2OST](https://github.com/Terasology/ChrisVolume2OST) - official game soundtrack by Chris Köbke - volume 2
* [Cities](https://github.com/Terasology/Cities) - procedural city placer and plot organizer, also places roads to connect cities. Later split into Static and Dynamic variants
* [ClimateConditions](https://github.com/Terasology/ClimateConditions) - A library module for managing temperature, humidity, and other climate factors
* [Climbables](https://github.com/Terasology/Climbables) - contains some utility blocks (and associated systems) for easier climbing of things
* [CombatSystem](https://github.com/Terasology/CombatSystem) - an advanced physics-based combat system
* [CommonWorld](https://github.com/Terasology/CommonWorld) - general world generation utility module
* [Compass](https://github.com/Terasology/Compass) - a simple compass
* [ComputerMonitors](https://github.com/Terasology/ComputerMonitors) - allows for the creation of functional multi-block monitors in-game - ModularComputers puts them to work!
* [Cooking](https://github.com/Terasology/Cooking) - cook stuff!
* [CopperAndBronze](https://github.com/Terasology/CopperAndBronze) - another era for throughout the ages, this time copper and bronze (comes after wood and stone)
* [CoreAdvancedAssets](https://github.com/Terasology/CoreAdvancedAssets) - a collection of assets using and combining features from various (core) modules
* [CoreAssets](https://github.com/Terasology/CoreAssets) - base assets extracted from Core
* [CoreRendering](https://github.com/Terasology/CoreRendering) - essential module for clients containing all basic rendering nodes
* [CoreSampleGameplay](https://github.com/Terasology/CoreSampleGameplay) - default gameplay with a few core modules enabled
* [CoreWorlds](https://github.com/Terasology/CoreWorlds) - Basic world generators and facets
* [CustomOreGen](https://github.com/Terasology/CustomOreGen) - library containing an ore distribution algorithm based on [JRoush's CustomOreGen](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1282294-1-4-6-v2-custom-ore-generation-updated-jan-5th)
* [DamagingBlocks](https://github.com/Terasology/DamagingBlocks) - allows blocks like lava to hurt the player
* [Dialogs](https://github.com/Terasology/Dialogs) - supports opening dialogs for interactions with NPCs and such
* [Drops](https://github.com/Terasology/Drops) - means to drop objects, create loot groups, and get random loot from a group on request
* [Durability](https://github.com/Terasology/Durability) - library module to support destructible items (wear and tear eventually breaks them)
* [DynamicCities](https://github.com/Terasology/DynamicCities) - variant of Cities that places then grows cities dynamically over time 
* [Economy](https://github.com/Terasology/Economy) - a module to simulate a basic supply & demand economy system
* [EdibleFlora](https://github.com/Terasology/EdibleFlora) - basic growth system providing plant stuff you can eat
* [EdibleSubstance](https://github.com/Terasology/EdibleSubstance) - allows substances to be tagged as edible and consumed
* [Equipment](https://github.com/Terasology/Equipment) - an equipment system with a character screen (`c`) showing stats and slots for various equipment types
* [EquipmentSmithing](https://github.com/Terasology/EquipmentSmithing) - acts as a bridge between Equipment and Smithing modules so that a player can forge weaponry and armor
* [EventualSkills](https://github.com/Terasology/EventualSkills) - a time-based skill system, a bit akin to EVE Online's system
* [Exoplanet](https://github.com/Terasology/Exoplanet) - a new world with a second "dimension" you can travel to through a portal
* [Explosives](https://github.com/Terasology/Explosives) - a library which allows things to explode
* [Fences](https://github.com/Terasology/Fences) - fences!
* [FlexibleMovement](https://github.com/Terasology/FlexibleMovement) - a movement implementation for FlexiblePathfinding (see below) (DISCLAIMER: this module is currently unstable)
* [FlexiblePathfinding](https://github.com/Terasology/FlexiblePathfinding) - a more flexible variant pathfinding framework
* [FlowingLiquids](https://github.com/Terasology/FlowingLiquids) - a more advanced flowing liquid system than SimpleLiquids - treats liquids as finite, makes them propagate, and current will even drag you around!
* [Fluid](https://github.com/Terasology/Fluid) - adds support for fluid in non-world situations (such as for storage in workstations)
* [FluidComputerIntegration](https://github.com/Terasology/FluidComputerIntegration) - allows for interactions with fluids via computers
* [FunnyBlocks](https://github.com/Terasology/FunnyBlocks) - cheese wheels and bowling pins - why not
* [Furnishings](https://github.com/Terasology/Furnishings/blob/develop/module.txt) - provides logic for furnishings like doors or chests
* [Genome](https://github.com/Terasology/Genome) - genetics WOO! Complete with DNA letters and mutating plants. Part of the Wood & Stone line-up
* [GooeyDefence](https://github.com/Terasology/GooeyDefence) - tower defense gameplay! Face down the hordes of Gooeys with defensive towers protecting a central shrine
* [GooeysQuests](https://github.com/Terasology/GooeysQuests) - spawn our mascot Gooey (console: `spawnPrefab gooey`) then `e` click to interact for commands and quests!
* [GooKeeper](https://github.com/Terasology/GooKeeper) - those wild Gooeys totally look like they'd be happy in some Zoo enclosures. Don't they? Then have visitors pay you!
* [GrowingFlora](https://github.com/Terasology/GrowingFlora) - organically growing (step by step) trees and such
* [Health](https://github.com/Terasology/Health) - basic health functionality, extracted from the engine / Core
* [HjerlHede](https://github.com/Terasology/HjerlHede) - a pure build module containing some Structure Template based buildings from the [Danish natural museum Hjerl Hede](http://hjerlhede.dk/en)
* [HumanoidCharacters](https://github.com/Terasology/HumanoidCharacters) - holds humanoid characters, to make you look different than a basic floating block (or an old monkey head!)
* [Hunger](https://github.com/Terasology/Hunger) - makes the player slowly gets hungry (needs actual GUI work and ways to then actually eat food though). Console `hungerCheck` for stats
* [Inferno](https://github.com/Terasology/Inferno) - live on in an underworld after death!
* [InGameHelp](https://github.com/Terasology/InGameHelp) - system for showing help in-game, default key `P`
* [InGameHelpAPI](https://github.com/Terasology/InGameHelpAPI) - separate API module for IGH to allow support but not forced activation for the help system
* [Inventory](https://github.com/Terasology/Inventory) - basic inventory originally bundled with the engine via Core - slot-based, no weight limits
* [IRLCorp](https://github.com/Terasology/IRLCorp) - Industrialized Reduction of Labor Corporation - Helping workmen everywhere
* [ItemPipes](https://github.com/Terasology/ItemPipes) - pipes! That can contain items!
* [ItemRendering](https://github.com/Terasology/ItemRendering) - a library for displaying "holographic" items in the world
* [JoshariasSurvival](https://github.com/Terasology/JoshariasSurvival) - formerly known as TerraTech - gameplay template for a machine-centric survival style
* [Journal](https://github.com/Terasology/Journal) - allows the player to use an in-game journal for gameplay notifications and such. Default toggle key `J`
* [Kallisti](https://github.com/Terasology/Kallisti) - library for embedding fantasy computer virtual machines in game engines. See also its [non-module variant](https://github.com/MovingBlocks/Kallisti)
* [KComputers](https://github.com/Terasology/KComputers) - computer implementation of Kallisti with Terasology-side content
* [Lakes](https://github.com/Terasology/Lakes) - add lakes to any enabled world generator, both surface lakes and subterranean ones (water and lava both!)
* [LegacyMusic](https://github.com/Terasology/LegacyMusic) - older music pieces predating the official soundtrack
* [LightAndShadow](https://github.com/Terasology/LightAndShadow) - main module for the Light & Shadow gameplay
* [LightAndShadowResources](https://github.com/Terasology/LightAndShadowResources) - IMMA FIRIN’ MAH LASR!! Art assets for the Light & Shadow concept
* [Lost](https://github.com/Terasology/Lost) - gameplay template for a survival / exploration focused setting
* [Machines](https://github.com/Terasology/Machines) - machine infrastructure library module
* [ManualLabor](https://github.com/Terasology/ManualLabor) - tools and logic for manual labor (digging, chopping, etc)
* [ManualLaborEventualSkills](https://github.com/Terasology/ManualLaborEventualSkills) - bridge module for adding EventualSkills to ManualLabor
* [MarkovChains](https://github.com/Terasology/MarkovChains) - Library module with some pseudo random math stuff
* [MasterOfOreon](https://github.com/Terasology/MasterOfOreon) - Master the Oreons, or others like them, from the throne-world of the Ancients! A menu command system, default show/hide key `O`
* [MawGooey](https://github.com/Terasology/MawGooey) - Introduces Gooey's 'odd' cousin to the game. Say hi, just don't get too close ..
* [Maze](https://github.com/Terasology/Maze) - a maze generator. Right-click with the provided maze tool on one block then again on another and a maze will generate between the two points (in multiple layers if the area is tall enough)
* [MedievalCities](https://github.com/Terasology/MedievalCities) - a set of building templates for a medieval era city
* [MetalRenegades](https://github.com/Terasology/MetalRenegades) - gameplay template set in a Wild West style world. GSOC 2019
* [Minerals](https://github.com/Terasology/Minerals) - a large collection of mineral blocks
* [Minesweeper](https://github.com/Terasology/Minesweeper) - a little game inside a game
* [Minimap](https://github.com/Terasology/Minimap) - a basic minimap. Show/hide with `M` by default and zoom with numpad `-` and `+`
* [MobileBlocks](https://github.com/Terasology/MobileBlocks) - supports blocks that can move their location based on some directions
* [ModularComputers](https://github.com/Terasology/ModularComputers) - central module for the creation of computers that themselves can have "hardware modules" of sorts added to them in-game to add interesting functionality
* [ModuleTestingEnvironment](https://github.com/Terasology/ModuleTestingEnvironment) - a testing framework for in-game tests on simulated server + client(s)
* [MoreLights](https://github.com/Terasology/MoreLights) - assorted illuminated blocks
* [MultiBlock](https://github.com/Terasology/MultiBlock) - supports the concept of multiple blocks being part of the same structure
* [MusicDirector](https://github.com/Terasology/MusicDirector) - allows music assets to be prepared for dynamic inclusion in appropriate contexts (like time of day)
* [NameGenerator](https://github.com/Terasology/NameGenerator) - can create random themed names for use by other modules, or via console using commands like `generateNameList 10`
* [NeoTTA](https://github.com/Terasology/NeoTTA) - experimental gameplay template with many modules focusing on crafting at various tiers (wood, stone, metals)
* [OreGeneration](https://github.com/Terasology/OreGeneration) - ore generation plugin system based on CustomOreGen (this one enables easy definition of what ores you want in a world)
* [Oreons](https://github.com/Terasology/Oreons) - little sentient cookie people! Don't do much yet. Place with `spawnPrefab OreonGuard` in the console (other types exist)
* [ParadIce](https://github.com/Terasology/ParadIce) - support for arctic environment creation
* [Pathfinding](https://github.com/Terasology/Pathfinding) - framework for pathfinding used by other modules
* [PhysicalStats](https://github.com/Terasology/PhysicalStats) - introduces a basic attributes system akin to traditional RPGs
* [PlantPack](https://github.com/Terasology/PlantPack) - more plants! Used by the Throughout the Ages gameplay
* [PolyWorld](https://github.com/Terasology/PolyWorld) - creates very neat island worlds based on the [map generating algorithm by Amit Patel of Red Blob Games](http://www-cs-students.stanford.edu/~amitp/game-programming/polygon-map-generation/)
* [Portals](https://github.com/Terasology/Portals) - allows placement of portal blocks that'll spawn Oreons `give portal`
* [PotentialEnergyDevices](https://github.com/Terasology/PotentialEnergyDevices) - A library for creating entities that build up potential energy
* [Potions](https://github.com/Terasology/Potions) - contains a set of assorted potions the player can consume to gain various effects
* [Projectile](https://github.com/Terasology/Projectile) - supported a variety of projectiles such as grenades and fireballs
* [Rails](https://github.com/Terasology/Rails) - railroads and trains! Press `E` to start a caboose or enter a cart. Use the wrench to attach carts
* [Sample](https://github.com/Terasology/Sample) - miscellaneous example content showcasing module usage
* [Scenario](https://github.com/Terasology/Scenario) - a scenario making toolset
* [Seasons](https://github.com/Terasology/Seasons) - adds seasons to the game
* [SegmentedPaths](https://github.com/Terasology/SegmentedPaths) - utility module for structured paths
* [Sensors](https://github.com/Terasology/Sensors) - used for sensing other entities, involved in collision detection and combat-y things
* [ShatteredPlanes](https://github.com/Terasology/ShatteredPlanes) - a world generator focused on canyons, sky islands, and other somewhat radical terrain features
* [Signalling](https://github.com/Terasology/Signalling) - circuitry implementation based on BlockNetwork, similar to redstone
* [SimpleFarming](https://github.com/Terasology/SimpleFarming) - easy to understand growing of foods
* [Smithing](https://github.com/Terasology/Smithing) - crafting for metal-based recipes
* [Soils](https://github.com/Terasology/Soils) - a small pack of different soil types
* [SoundyGenetics](https://github.com/Terasology/SoundyGenetics) - advanced genetics system. GSOC 2019
* [Spawning](https://github.com/Terasology/Spawning) - split out from Portals to serve as general utility for anything needing stuff to spawn
* [StaticCities](https://github.com/Terasology/StaticCities) - variant of Cities that places all cities during world generation without later dynamic growth
* [StoneCrafting](https://github.com/Terasology/StoneCrafting) - introduces stone based crafting through workstations
* [StructuralResources](https://github.com/Terasology/StructuralResources) - a set of structural shapes suitable for buildings and such
* [StructureTemplates](https://github.com/Terasology/StructureTemplates) - a system for placing structures as per some template
* [SubstanceMatters](https://github.com/Terasology/SubstanceMatters) - library for the definition and usage of materials in various contexts, such as tools with dynamic looks based on material
* [SurfaceFacets](https://github.com/Terasology/SurfaceFacets) - world generation enhancements related to surfaces
* [Tasks](https://github.com/Terasology/Tasks) - allows for the definition of tasks/quests
* [Thirst](https://github.com/Terasology/Thirst) - thirst as a survival feature in the form of a status bar
* [Valentines](https://github.com/Terasology/Valentines) - What is love? Gooey don't hurt me, don't hurt me, no more ... ♫
* [WeatherManager](https://github.com/Terasology/WeatherManager) - simple weather foundation, tracks whether, maintains a single cloud layer, and so on 
* [WildAnimals](https://github.com/Terasology/WildAnimals) - a module containing animals, initially a deer you can spawn in-world via console with `spawnPrefab deer` then watch wander idly
* [WildAnimalsGenome](https://github.com/Terasology/WildAnimalsGenome) - bridge module using Genome to bring more advanced features to WildAnimals
* [WoodAndStone](https://github.com/Terasology/WoodAndStone) - big content module including "from scratch" crafting, starting with wood here
* [WoodAndStoneCraftingJournal](https://github.com/Terasology/WoodAndStoneCraftingJournal) - a Journal bridging module for WoodCrafting and StoneCrafting (no direct relation with the WoodAndStone module)
* [WoodCrafting](https://github.com/Terasology/WoodCrafting) - basic woodcrafting, recipes, and crafting stations
* [Workstation](https://github.com/Terasology/Workstation) - workstations offer a way to use blocks in-world for advanced purposes
* [WorkstationCrafting](https://github.com/Terasology/WorkstationCrafting) - an extension to Workstation focused on crafting more advanced recipe-based stuff
* [WorkstationInGameHelp](https://github.com/Terasology/WorkstationInGameHelp) - bridging module to bring in-game help to workstation screens
* [WorldlyTooltip](https://github.com/Terasology/WorldlyTooltip) - a little tooltip that shows you what you're looking at (hold `alt` for debug details)
* [WorldlyTooltipAPI](https://github.com/Terasology/WorldlyTooltipAPI) - API module for WorldlyTooltip for better dependency management

Some of the modules in action:

![Terasology](images/PopulatedVillage.jpg "Terasology")

## Other Modules

* [AdditionalWorlds](https://github.com/Terasology/AdditionalWorlds) - container module for more worlds
* [CoreBlocks](https://github.com/Terasology-Archived/CoreBlocks) - base blocks extracted from Core. Replaced by [CoreAssets](https://github.com/Terasology/CoreAssets)
* [Malicious](https://github.com/Terasology/Malicious) - a series of module security tests to check that modules cannot do naughty things when running
* [MarcinScIncubator](https://github.com/Terasology-Archived/MarcinScIncubator) - parking lot for tools used in @MarcinSc's many modules without a more explicit home yet. Currently archived
* [SimpleLiquids](https://github.com/Terasology/SimpleLiquids) - lets water propagate in the world - beware of floods! Currently archived
* [ThroughoutTheAges](https://github.com/Terasology/ThroughoutTheAges) - gameplay module for a large content series letting you slowly climb a tech tree to improve your available tools, foods, and so on. Currently buggy and superseded by [NeoTTA](https://github.com/Terasology/NeoTTA)

# Tutorials

These modules are more likely for some time to be for developers/modders to learn about different kinds of content and systems, not for players to actually learn stuff in-game. That's usually handled by actual content modules including their own in-game help system.

* [TutorialAssetSystem](https://github.com/Terasology/TutorialAssetSystem) - a tutorial module covering our asset system, see also its [wiki](https://github.com/Terasology/TutorialAssetSystem/wiki)
* [TutorialBehaviors](https://github.com/Terasology/TutorialBehaviors) - a tutorial module covering our behavior tree system
* [TutorialBlockFamily](https://github.com/Terasology/TutorialBlockFamily) - provides understanding of the block family system
* [TutorialDynamicCities](https://github.com/Terasology/TutorialDynamicCities) - GSOC 2016 project tutorial / docs. Covers how dynamic cities function. [Shared wiki with DynamicCities](https://github.com/Terasology/DynamicCities/wiki)
* [TutorialEntitySystem](https://github.com/Terasology/TutorialEntitySystem) - information on the Entity Component System (ECS)
* [TutorialEventsInteractions](TutorialEventsInteractions) - covers using events in the Entity Component System
* [TutorialI18n](https://github.com/Terasology/TutorialI18n) - covers i18n, which lowers language barriers
* [TutorialMinimalEngineDemo](https://github.com/Terasology/TutorialMinimalEngineDemo) - A module demonstrating how to build a minimal "game" with the barebone Terasology engine
* [TutorialMultiplayerExtras](https://github.com/Terasology/TutorialMultiplayerExtras) - covers using the ECS in multiplayer
* [TutorialNui](https://github.com/Terasology/TutorialNui) - GSOC 2016 project tutorial / docs. Includes details both on NUI itself as well as its editor in the [wiki](https://github.com/Terasology/TutorialNui/wiki)
* [TutorialParticleSystem](https://github.com/Terasology/TutorialParticleSystem) - a tutorial module covering the particle system
* [TutorialPathfinding](https://github.com/Terasology/TutorialPathfinding) - tutorial that outlines several pathfinding topics including floor highlighting and paths
* [TutorialProfiling](https://github.com/Terasology/TutorialProfiling) - a performance focused tutotial module
* [TutorialQuests](https://github.com/Terasology/TutorialQuests) - Provides sample quests based on the Tasks module
* [TutorialSectors](https://github.com/Terasology/TutorialSectors) - a tutorial module covering our sector system (separated entity pools based on geographic location and differing entity scope)
* [TutorialTelemetry](https://github.com/Terasology/TutorialTelemetry) - telemetry tutorial for tracking metrics and gathering useful info
* [TutorialWorldGeneration](https://github.com/Terasology/TutorialWorldGeneration) - a world generation tutorial module, goes with a guide in its [wiki](https://github.com/Terasology/TutorialWorldGeneration/wiki)

