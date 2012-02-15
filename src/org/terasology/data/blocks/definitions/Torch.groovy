package org.terasology.data.blocks.definitions

import org.terasology.model.blocks.Block.BLOCK_FORM

/**
 * Torches catch on fire and stuff. On purpose!
 */
block {
    version = 1
    blockform = BLOCK_FORM.DEFAULT

    translucent = true
    penetrable = true
    allowBlockAttachment = false

    luminance = 15
    hardness = 1
    shape {
        mode = "AttachToSurface"
        sides = "TorchWall"
        bottom = "TorchGrounded"
    }
}