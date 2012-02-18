package org.terasology.data.blocks.definitions

/**
 * Brick material stairs
 */
block {
    version = 1
    faces {
        all = "Brick"
    }
    shape {
        mode = "HorizontalRotation"
        sides = "Stair"
    }

    hardness = 8

    physics {
        mass = 128000
    }
}