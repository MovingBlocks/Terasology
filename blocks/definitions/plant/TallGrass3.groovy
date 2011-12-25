import com.github.begla.blockmania.model.blocks.Block.BLOCK_FORM
import com.github.begla.blockmania.model.blocks.Block.COLOR_SOURCE

/**
 * Tall grass variety 3 (higher number = taller)
 */
block {
    version = 1

    blockform = BLOCK_FORM.BILLBOARD
    colorsource = COLOR_SOURCE.FOLIAGE_LUT
    colorOffset = [0.9f, 0.9f, 0.9f, 1.0f]

    translucent = true
    penetrable = true

    waving = true
    hardness = 1
}