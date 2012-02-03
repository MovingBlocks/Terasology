package org.terasology.data.blocks.definitions.plant.leaf

import org.terasology.model.blocks.Block.COLOR_SOURCE

/**
 * This is a leaf, which can exist in the world just as a block of its own
 * However, more likely it will be used by reference by a Tree
 */
block {
    version = 1

    faces.all = "GreenLeaf"

    hardness = 2

    translucent = true

    colorsource = COLOR_SOURCE.FOLIAGE_LUT
    colorOffset = [1.0f, 0.8f, 0.8f, 1.0f]
}