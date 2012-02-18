package org.terasology.data.blocks.definitions.plant

/**
 * A Cactus is a Plant, but not a Tree. All plants grow but trees additionally have separate leaves
 * In theory fancy cacti could possibly be L-system based? Not sure where L-system stuff should go
 */
block {
    version = 1
    shape = "Cactus"
    faces {
        top = "CactusTop"
        bottom = "CactusBottom"
    }

    // TODO: This is quite weird actually but necessary at the moment
    translucent = true
}