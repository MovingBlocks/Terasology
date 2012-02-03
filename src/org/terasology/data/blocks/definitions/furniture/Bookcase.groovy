package org.terasology.data.blocks.definitions.furniture

/**
 * A bookcase only has books on the front. Well, at least this one does, for now
 */
block {
    version = 1

    // Graphics
    faces {
        topbottom = "ChestTopBottom"
        left = "ChestTopBottom"
        right = "ChestTopBottom"
        back = "ChestTopBottom"
    }

    // MetaBlock assignment - where to look for more info on a specific instance of this block
    meta = Bookcase // Prolly will need a generic Container, of which Chests can hold some things, Bookcases others
}