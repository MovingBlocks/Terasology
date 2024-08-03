Minecraft was a smash hit and received a massive amount of support from a community, far exceeding what it was envisioned for, which lead to lackluster support for what the community wanted as fans could not directly improve the engine. Modding became minecraft largest community, but received no official support, thus mods needed to be updated at every release. Which can cause whole game types and communities to disappear if a mod cannot remain up to date. 

Terasology's goal is to be a solid, stable, and extremely extensible voxel game engine.



For example:

0. A mod API is our main and foremost feature. For end users all module adding, updating, and downloading is handled through a simple GUI at world creations. Modules are automatically added at server join. Gameplay Templates exist for a tested and packaged experience.

0. The creation of modules is simple, and if  behavior already exists, in libraries or frameworks, no java code is needed. Modules work across many [versions](http://semver.org/), and dependencies are automatically handled. Json is used heavily.

0. Organized, unified, but still independent. There is an official forum, IRC, and module repo. Content is unified: plugins, ResourcePacks, libraries, frameworks are just types of modules, and thus are accessed through the official module repo. However, the module repo is git based so it can be easily duplicated or forked, for decentralization. There is no login servers as each server manages the accounts themselves. Clients generate a SSH key at server join as form of Identity. However, a login module can be created for a centralized login server. 

0. A tiny core, all content should be added using modules. Freedom of gameplay

0. Infinite height and depth, commonly known as Cubic Chunks

0. Blocks and block shapes are separate. Once you define a shape, all blocks will have that variation. 

0. 65,000 IDs for biomes, A biome is defined per grid space, allowing for vertical biomes.

0. A better lighting engine. Includes shaders by default, torches can give off light while placed, held, or dropped.

0. Multithreaded. (Rendering is single threaded because of openGL, but this could change if a Rendering Wizard shows up)

0. Open source and gratis. All currently existing modules as well, but the licenses is to be decided by the Author. 

0. Community controlled and community ran. 

0. It's in Java.

__For Mod Developers__ 

A selection of libraries and frameworks has been created, to aid compatibility, and reduce duplication of work. Such as defining ores, common plants and how they grow, climate and weather, networking between blocks, how ores spawn etc. Modules are a good source of code reference, for seasoned and budding mod developers alike. A list of ready to use modules,libraries, and frameworks can be found in the [module repo](https://github.com/Terasology/)

# Gameplay and Module Management

__For the average user__

Switching between gameplay templates (a collection of modules, like a Minecraft modpack), is as simple as choosing between survival, creative, or hardcore in Minecraft. That is to say, it's completely handled through GUI with no external tools needed, and is managed per-world. This holds true for joining servers as well, no user setup needed.

![](https://i.imgur.com/OpmgYGP.png)
  

__For the advanced user__

There exists a module button next to the template dropdown box, this allows for customizing any template that is currently selected. Allowing you to remove or add modules. Modules can be updated and downloaded from the [module repo](https://github.com/Terasology/) from here. Dependencies are automatically handled. Modules not in repo can be enabled here after being placed in the correct folder. 

![](https://i.imgur.com/GTzNKiL.png)

#  Converting a Minecraft mod to a Terasology module

_note: this is a preliminary  guide, more information can be found in the wiki_

First get a working [dev environment](https://github.com/MovingBlocks/Terasology/wiki/Dev-Setup) and continue on to creating or downloading a module. 


___
[The structure of a module](https://github.com/MovingBlocks/Terasology/wiki/Modding-Guide#structure-of-a-mod)
___ 

textures can be placed in _ModuleName/assets/blockTiles/auto_ 

where a basic block with properties inherited is created. 

[Example](https://github.com/Terasology/Minerals/tree/master/assets/blockTiles/auto)
___

placing  texture(s) in _ModuleName/assets/blockTiles/fancy_ 

and creating a simple json file of _ModuleName/assets/blocks/BlockName.block_

allows properties to be set, such as the shape it comes in, its display name, categories it's in, hardness, the textures for each sides, its mass,translucent, if you can go through it, if you can target it, its tint, its prefab, debrisOnDestroy, if it waves in the wind, color source, its rotation, if you can climb it, if it drops when you break it or if you directly pick it up, if it casts a shadow etc. 

[Example](https://github.com/Terasology/Soils/tree/master/assets/blockTiles/fancy)

[Example](https://github.com/Terasology/Soils/tree/master/assets/blocks/fancy)
___
creating a file in _ModuleName/assets/prefabs/BlockOrItemName.prefab_ 

allows for the block or item to store data, becoming an entity. (as well as advanced data) Such as a chests ability to store items, and retain them inside when broken. Or torches needing a block to be on. Water being unbreathable, how items damage other items (like tools), the icons they use. Chests inventory being public to everyone, the sound it plays, the GUI screen it uses.  

[Example](https://github.com/Terasology/JoshariasSurvival/tree/master/assets/prefabs)
    
____

**Module.txt**

This is a textfile in the root of the module directory that gives module stats, and helps do the magic. 

It gives modules: an internal name, a displayed name, the Authors names, a short description, listing of dependencies so they can automatically be pulled, and so forth. 

[JoshariasSurvival](https://github.com/Terasology/JoshariasSurvival/blob/master/module.txt)  , a higher level module for example. 

'Josharias Survival' the game play Is  fairly complex. 'JoshariasSurvival' The module is not. JoshariasSurvival is more or less a meta package. It contains few content of its own, and actually calls Libraries and other modules as dependecies. All modules in Josharias Survival can be mixed and matched  with other modules, but JoshariasSurvival automatically pull all required modules for a set up and tested play style, a gameplay template, or in Minecraft terms, a modpack. 

The __"isGameplay" : "true",__  Causes this module to show up in the Gameplay template dropdown, as it has everything in order to be played as is. Things like Mineral Library or Hunger have this set to false as they are not useful or playable on their own. 


    

     


