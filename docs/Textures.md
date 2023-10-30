This page should give you an overview of how Terasology uses textures and how you can use them on your own.

# Location
As textures are programme Assets, they can be found in the `asset` folder of the main programme or some
modules. The different textures are then ordered by their intended usage, e.g. `blockTiles` or model textures.

# BlockTiles (subfolder)
A very basic use of textures are the _blockTiles_, the textures for single blocks. They can be found in
`src/main/resources/assets/blockTiles` or in the `blockTiles` subfolder of module's `asset` folder.

At the moment the size of block textures is restricted to **16x16 pixels**. They are stored as **.png** files and can
 contain transparency, e.g. for colored Glass.

Block textures need to have the same name as the corresponding JSON block definition to be auto loaded.

# Textures (subfolder)
Any textures that are no block tiles are located in `src/main/resources/assets/textures` or the corresponding mod
directories. You can find several types of image files in there:

* _color gradients_ for grass or foliage
* _GUI icons and backgrounds_ - elements assigned to the user interface, such as icons, inventory backgrounds, ...
* _Skybox textures_
* _Effect textures_ such as breaking a block or the overlayed vignette
* _model textures_ such for the monkey heads
    > Note: Models and its textures/materials should be sourced out to modules.

# Override
Because of the _Override Functionality_ for mods it is possible to override core textures. For more information see
 [Deltas and Overrides](https://github.com/Terasology/TutorialAssetSystem/wiki/Deltas-and-Overrides).

 Since the texture that is loaded first is used in the game, the override process is not deterministic at the moment
 and one cannot guarantee which texture will take effect.

# Known issues
* Using greyscale textures for grass or foliage works well for placed blocks (color grading),
but the icons still show up in grey color.
* `effects.png` cannot be overriden because its early loaded by the shader. Therefore,
custom texture packs need to use the default segmentation for `grassSide` to get the color grading right.

# Related links
* [Asset System Tutorial](https://github.com/Terasology/TutorialAssetSystem/wiki)
