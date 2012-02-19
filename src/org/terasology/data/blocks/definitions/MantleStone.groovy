package org.terasology.data.blocks.definitions

/**
 * MantleStone is our bedrock of sorts - the bottom layer of the world. It is indestructible
 */
block {
    version = 1
    shape = "cube"
    hardness = 0

    physics {
        mass = 256000
    }
}