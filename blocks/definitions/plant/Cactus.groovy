import org.terasology.model.blocks.Block.BLOCK_FORM

/**
 * A Cactus is a Plant, but not a Tree. All plants grow but trees additionally have separate leaves
 * In theory fancy cacti could possibly be L-system based? Not sure where L-system stuff should go
 */
block {
    version = 1

    faces {
        top = "CactusTop"
        bottom = "CactusBottom"
    }

    blockform = BLOCK_FORM.CACTUS
    disableTessellation = true

    // TODO: This is quite weird actually but necessary at the moment
    translucent = true
}