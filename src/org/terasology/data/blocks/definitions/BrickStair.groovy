package org.terasology.data.blocks.definitions

/**
 * Brick material stairs
 */
block {
    version = 1
    alignment = "HorizontalDirection"
    faces {
        all = "Brick"
    }
    shape = "Stair"

    hardness = 8

    physics {
        mass = 128000
    }
}