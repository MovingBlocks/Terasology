package org.terasology.data.blocks.definitions.furniture

/**
 * A chest is interesting for two reasons: It is asymmetrical (front texture differs) and should store stuff
 */
block {
    version = 1

    // Graphics
    faces {
        sides = "ChestSides"
        front = "ChestFront"
        topbottom = "ChestTopBottom"
    }

    // MetaBlock assignment - where to look for more info on a specific instance of this block
    meta = Chest
}