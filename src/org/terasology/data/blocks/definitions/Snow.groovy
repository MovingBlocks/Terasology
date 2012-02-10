package org.terasology.data.blocks.definitions

/**
 * Snow is a Dirt block covered by Snow, just like grass.
 * Unlike Grass, it might be possible to have a SnowBlock as well
 */
block {
    version = 1
    shape = "cube"

    faces {
        sides = "SnowSide"
        bottom = "Dirt"
    }
}