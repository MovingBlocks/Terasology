package org.terasology.data.blocks.definitions

import org.terasology.model.blocks.Block.BLOCK_FORM

/**
 * Torches catch on fire and stuff. On purpose!
 */
block {
    alignment="SurfaceAligned"
    version = 1
    blockform = BLOCK_FORM.DEFAULT

    translucent = true
    penetrable = true
    allowBlockAttachment = false

    luminance = 15
    hardness = 1

    sides {
        shape = "TorchWall"
    }
    bottom {
        shape = "TorchGrounded"
    }
}