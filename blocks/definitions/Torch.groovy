import org.terasology.model.blocks.Block.BLOCK_FORM

/**
 * Torches catch on fire and stuff. On purpose!
 */
block {
    version = 1

    blockform = BLOCK_FORM.BILLBOARD

    translucent = true
    penetrable = true
    allowBlockAttachment = false

    luminance = 15
    hardness = 1
}