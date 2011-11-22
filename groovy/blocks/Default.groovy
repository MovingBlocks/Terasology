import com.github.begla.blockmania.blocks.Block
import com.github.begla.blockmania.blocks.Block.BLOCK_FORM
import com.github.begla.blockmania.blocks.Block.COLOR_SOURCE
import com.github.begla.blockmania.blocks.BlockManager
import javax.vecmath.Vector2f
import javax.vecmath.Vector4f

def BlockManager bm = blockManager;

// The air we breathe
bm.addBlock(new Block().withTitle("Air").withId((byte) 0).withDisableTessellation(true).withTranslucent(true).withInvisible(true).withBypassSelectionRay(true).withPenetrable(true).withCastsShadows(false).withRenderBoundingBox(false).withAllowBlockAttachment(false).withHardness((byte) 0))
// <--

// Etc.
bm.addBlock(new Block().withTitle("Bookshelf").withId((byte) 27).withTextureAtlasPosTopBottom(new Vector2f(6, 5)).withTextureAtlasPosMantle(new Vector2f(3, 2)))
// <--

// Liquids
bm.addBlock(new Block().withTitle("Water").withId((byte) 9).withTextureAtlasPos(new Vector2f(15, 12)).withTranslucent(true).withPenetrable(true).withRenderBoundingBox(false).withHardness((byte) -1).withAllowBlockAttachment(false).withBypassSelectionRay(true).withCastsShadows(false).withLiquid(true).withBlockForm(BLOCK_FORM.LOWERED_BLOCK))

bm.addBlock(new Block().withTitle("Lava").withId((byte) 10).withTextureAtlasPos(new Vector2f(15, 15)).withTranslucent(true).withPenetrable(true).withRenderBoundingBox(false).withHardness((byte) -1).withAllowBlockAttachment(false).withBypassSelectionRay(true).withCastsShadows(false).withLiquid(true).withBlockForm(BLOCK_FORM.LOWERED_BLOCK))
// <--

// Ice
bm.addBlock(new Block().withTitle("Ice").withId((byte) 8).withTextureAtlasPos(new Vector2f(3,4)).withTranslucent(true))
// <--

// Glass
bm.addBlock(new Block().withTitle("Glass").withId((byte) 26).withTextureAtlasPos(new Vector2f(2, 2)).withTranslucent(true))
// <--

// Soil
bm.addBlock(new Block().withTitle("Dirt").withId((byte) 1).withTextureAtlasPos(new Vector2f(2, 0)))

bm.addBlock(new Block().withTitle("Grass").withId((byte) 6).withTextureAtlasPosMantle(new Vector2f(3, 0)).withTextureAtlasPos(Block.SIDE.TOP, new Vector2f(0, 0)).withTextureAtlasPos(Block.SIDE.BOTTOM, new Vector2f(2, 0)).withColorSource(COLOR_SOURCE.COLOR_LUT))

bm.addBlock(new Block().withTitle("Snow").withId((byte) 7).withTextureAtlasPosMantle(new Vector2f(4, 4)).withTextureAtlasPos(Block.SIDE.TOP, new Vector2f(2, 4)).withTextureAtlasPos(Block.SIDE.BOTTOM, new Vector2f(2, 0)))

bm.addBlock(new Block().withTitle("Sand").withId((byte) 2).withTextureAtlasPos(new Vector2f(2, 1)))
// <--

// Wood
bm.addBlock(new Block().withTitle("Wood").withId((byte) 4).withTextureAtlasPos(new Vector2f(4, 0)))

bm.addBlock(new Block().withTitle("Tree trunk").withId((byte) 5).withTextureAtlasPosTopBottom(new Vector2f(5, 1)).withTextureAtlasPosMantle(new Vector2f(4, 1)))
// <--

// Stone
bm.addBlock(new Block().withTitle("Stone").withId((byte) 3).withTextureAtlasPos(new Vector2f(1, 0)))

bm.addBlock(new Block().withTitle("Cobble stone").withId((byte) 19).withTextureAtlasPos(new Vector2f(0, 1)))

bm.addBlock(new Block().withTitle("Hard stone").withId((byte) 18).withTextureAtlasPos(new Vector2f(1, 1)).withHardness((byte) -1))

bm.addBlock(new Block().withTitle("Brick").withId((byte) 20).withTextureAtlasPos(new Vector2f(7, 0)))
// <--

// Billboards
bm.addBlock(new Block().withTitle("Red flower").withId((byte) 16).withTextureAtlasPos(new Vector2f(13, 0)).withTranslucent(true).withPenetrable(true).withTranslucent(true).withColorSource(COLOR_SOURCE.FOLIAGE_LUT).withBlockForm(BLOCK_FORM.BILLBOARD).withAllowBlockAttachment(false))

bm.addBlock(new Block().withTitle("Yellow flower").withId((byte) 17).withTextureAtlasPos(new Vector2f(12, 0)).withTranslucent(true).withPenetrable(true).withColorSource(COLOR_SOURCE.FOLIAGE_LUT).withBlockForm(BLOCK_FORM.BILLBOARD).withAllowBlockAttachment(false))

bm.addBlock(new Block().withTitle("High grass").withId((byte) 13).withTextureAtlasPos(new Vector2f(12, 11)).withTranslucent(true).withPenetrable(true).withColorSource(COLOR_SOURCE.FOLIAGE_LUT).withBlockForm(BLOCK_FORM.BILLBOARD).withAllowBlockAttachment(false))

bm.addBlock(new Block().withTitle("Medium high grass").withId((byte) 14).withTextureAtlasPos(new Vector2f(13, 11)).withTranslucent(true).withPenetrable(true).withColorSource(COLOR_SOURCE.FOLIAGE_LUT).withBlockForm(BLOCK_FORM.BILLBOARD).withAllowBlockAttachment(false))

bm.addBlock(new Block().withTitle("Large high grass").withId((byte) 15).withTextureAtlasPos(new Vector2f(14, 11)).withTranslucent(true).withPenetrable(true).withColorSource(COLOR_SOURCE.FOLIAGE_LUT).withBlockForm(BLOCK_FORM.BILLBOARD).withAllowBlockAttachment(false))
// <--

// Cacti
bm.addBlock(new Block().withTitle("Cactus").withId((byte) 28).withTextureAtlasPosTopBottom(new Vector2f(5, 4)).withTextureAtlasPosMantle(new Vector2f(6, 4)).withBlockForm(BLOCK_FORM.CACTUS).withDisableTessellation(true).withTranslucent(true))
// <--

// Minerals
bm.addBlock(new Block().withTitle("Coal").withId((byte) 21).withTextureAtlasPos(new Vector2f(2, 2)))

bm.addBlock(new Block().withTitle("Diamond").withId((byte) 25).withTextureAtlasPos(new Vector2f(2, 3)))

bm.addBlock(new Block().withTitle("Gold").withId((byte) 24).withTextureAtlasPos(new Vector2f(0, 2)))

bm.addBlock(new Block().withTitle("Red stone").withId((byte) 23).withTextureAtlasPos(new Vector2f(3, 3)))

bm.addBlock(new Block().withTitle("Silver").withId((byte) 22).withTextureAtlasPos(new Vector2f(1, 2)))
// <--

// Light sources
bm.addBlock(new Block().withTitle("Torch").withId((byte) 29).withTextureAtlasPos(new Vector2f(0, 5)).withTranslucent(true).withPenetrable(true).withBlockForm(BLOCK_FORM.BILLBOARD).withLuminance((byte) 15).withAllowBlockAttachment(false))
// <--

// Leafs
bm.addBlock(new Block().withTitle("Leaf").withId((byte) 11).withTextureAtlasPos(new Vector2f(4, 3)).withTranslucent(true).withAllowBlockAttachment(false).withColorSource(COLOR_SOURCE.FOLIAGE_LUT))
bm.addBlock(new Block().withTitle("Red leaf").withId((byte) 30).withTextureAtlasPos(new Vector2f(4, 3)).withTranslucent(true).withAllowBlockAttachment(false).withColorSource(COLOR_SOURCE.FOLIAGE_LUT).withColorOffset(new Vector4f(1.0f, 0.8f, 0.8f, 1.0f)))
bm.addBlock(new Block().withTitle("Dark leaf").withId((byte) 12).withTextureAtlasPos(new Vector2f(4, 3)).withTranslucent(true).withAllowBlockAttachment(false).withColorSource(COLOR_SOURCE.FOLIAGE_LUT).withColorOffset(new Vector4f(0.5f, 0.5f, 0.5f, 1.0f)))
// <--