# Module.txt

## Contents

* _id_ - Machine-readable name of the module.  Used by the terasology engine and other modules to refer to the module.
* _version_ - Version of the module. See [Module Versioning](Release-Modules.md#versioning)
* _displayName_ - The name of the module isplayed to end-users
* _description_ - Human-readable description of the module.
* _dependencies_ - List of ids and minVersions of modules needed for this module to work. See [Module-Dependencies](Module-Dependencies.md)
* _defaultWorldGenerator_ - The default world generator to use for this module.

### Ways to categorize your module:

* _serverSideOnly_ - only used on servers
* _isGameplay_ - defines a game type/mode (mod pack) that may depend on a large number of modules and forces a particular style of play. Unlikely to work well with other gameplay modules. Example: WoodAndStone, LightAndShadow
* _isAugmentation_ - some sort of plug and play content that can be enabled and seen/played, but doesn't force particular gameplay. Could work in combination with a gameplay module. Example: NightTerrors* isAsset - plain art / passive content module, possibly with basic block/model definitions. Example: LASR, Minerals, Soils
* _isWorld_ - provides world generation features.  Example: AnotherWorld
* _isLibrary_ - active content for use by other modules. Cannot be used by itself.  Typically Component Systems.  Example: BlockNetwork
* _isSpecial_ - Special-purpose modules.  Core, Sample, Malicious

## Example


    {
        "id" : "CoreSampleGameplay",
        "version" : "2.0.0-SNAPSHOT",
        "displayName" : "Core Gameplay",
        "description" : "Minimal gameplay template. Little content but a few starting items.",
        "dependencies" : [
            {"id": "Core", "minVersion": "2.0.0"}
        ],
        "isGameplay" : "true",
        "defaultWorldGenerator" : "Core:FacetedPerlin"
    }

## More information

[Gestalt Modules](https://github.com/MovingBlocks/gestalt/wiki/Modules) - In-depth discussion of Modules (non-Terasology-specific)