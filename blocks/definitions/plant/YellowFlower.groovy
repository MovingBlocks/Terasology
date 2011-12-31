import org.terasology.model.blocks.Block.BLOCK_FORM
import org.terasology.model.blocks.Block.COLOR_SOURCE

/**
 * A totally beautiful yellow flower that might get some actual plant stats later
 */
block {
    version = 1

    blockform = BLOCK_FORM.BILLBOARD
    colorsource = COLOR_SOURCE.FOLIAGE_LUT

    translucent = true
    penetrable = true

    hardness = 1
}