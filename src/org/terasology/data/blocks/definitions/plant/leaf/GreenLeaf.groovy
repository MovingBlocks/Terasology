package org.terasology.data.blocks.definitions.plant.leaf

import org.terasology.model.blocks.Block.COLOR_SOURCE

/**
 * This is a leaf, which can exist in the world just as a block of its own
 * However, more likely it will be used by reference by a Tree
 */
block {
    version = 1

    translucent = true

    hardness = 1

    colorsource = COLOR_SOURCE.FOLIAGE_LUT
}