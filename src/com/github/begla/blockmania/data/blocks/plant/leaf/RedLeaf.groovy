package com.github.begla.blockmania.data.blocks.plant.leaf

import com.github.begla.blockmania.blocks.Block.COLOR_SOURCE

/**
 * This is a leaf, which can exist in the world just as a block of its own
 * However, more likely it will be used by reference by a Tree
 */
block {
    version = 1

    faces.all = "GreenLeaf"

    translucent = true

    colorsource = COLOR_SOURCE.COLOR_LUT
    colorOffset = [1.0f, 0.8f, 0.8f, 1.0f]
}