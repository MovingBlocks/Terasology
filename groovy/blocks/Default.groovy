import com.github.begla.blockmania.blocks.Block
import com.github.begla.blockmania.blocks.Block.BLOCK_FORM
import com.github.begla.blockmania.blocks.Block.COLOR_SOURCE
import com.github.begla.blockmania.blocks.BlockManager
import org.lwjgl.util.vector.Vector2f
import org.lwjgl.util.vector.Vector4f

def BlockManager bm = blockManager;

// The air we breathe
bm.addBlock(new Block().setTitle("Air").setId((byte) 0).setDisableTesselation(true).setTranslucent(true).setInvisible(true).setBypassSelectionRay(true).setPenetrable(true).setCastsShadows(false).setRenderBoundingBox(false).setAllowBlockAttachment(false).setHardness((byte) 0))
// <--

// Etc.
bm.addBlock(new Block().setTitle("Bookshelf").setId((byte) 27).setTextureAtlasPosTopBottom(new Vector2f(6, 5)).setTextureAtlasPosMantle(new Vector2f(3, 2)))
// <--

// Liquids
bm.addBlock(new Block().setTitle("Water").setId((byte) 9).setTextureAtlasPos(new Vector2f(15, 12)).setTranslucent(true).setPenetrable(true).setRenderBoundingBox(false).setHardness((byte) -1).setAllowBlockAttachment(false).setBypassSelectionRay(true).setCastsShadows(false).setLiquid(true).setBlockForm(BLOCK_FORM.LOWERED_BLOCK))

bm.addBlock(new Block().setTitle("Lava").setId((byte) 10).setTextureAtlasPos(new Vector2f(15, 15)).setTranslucent(true).setPenetrable(true).setRenderBoundingBox(false).setHardness((byte) -1).setAllowBlockAttachment(false).setBypassSelectionRay(true).setCastsShadows(false).setLiquid(true).setBlockForm(BLOCK_FORM.LOWERED_BLOCK))
// <--

// Ice
bm.addBlock(new Block().setTitle("Ice").setId((byte) 8).setTextureAtlasPos(new Vector2f(3,4)).setTranslucent(true))
// <--

// Glass
bm.addBlock(new Block().setTitle("Glass").setId((byte) 26).setTextureAtlasPos(new Vector2f(2, 2)).setTranslucent(true))
// <--

// Soil
bm.addBlock(new Block().setTitle("Dirt").setId((byte) 1).setTextureAtlasPos(new Vector2f(2, 0)))

bm.addBlock(new Block().setTitle("Grass").setId((byte) 6).setTextureAtlasPosMantle(new Vector2f(3, 0)).setTextureAtlasPos(Block.SIDE.TOP, new Vector2f(0, 0)).setTextureAtlasPos(Block.SIDE.BOTTOM, new Vector2f(2, 0)).setColorSource(COLOR_SOURCE.COLOR_LUT))

bm.addBlock(new Block().setTitle("Snow").setId((byte) 7).setTextureAtlasPosMantle(new Vector2f(4, 4)).setTextureAtlasPos(Block.SIDE.TOP, new Vector2f(2, 4)).setTextureAtlasPos(Block.SIDE.BOTTOM, new Vector2f(2, 0)))

bm.addBlock(new Block().setTitle("Sand").setId((byte) 2).setTextureAtlasPos(new Vector2f(2, 1)))
// <--

// Wood
bm.addBlock(new Block().setTitle("Wood").setId((byte) 4).setTextureAtlasPos(new Vector2f(4, 0)))

bm.addBlock(new Block().setTitle("Tree trunk").setId((byte) 5).setTextureAtlasPosTopBottom(new Vector2f(5, 1)).setTextureAtlasPosMantle(new Vector2f(4, 1)))
// <--

// Stone
bm.addBlock(new Block().setTitle("Stone").setId((byte) 3).setTextureAtlasPos(new Vector2f(1, 0)))

bm.addBlock(new Block().setTitle("Cobble stone").setId((byte) 19).setTextureAtlasPos(new Vector2f(0, 1)))

bm.addBlock(new Block().setTitle("Hard stone").setId((byte) 18).setTextureAtlasPos(new Vector2f(1, 1)).setHardness((byte) -1))

bm.addBlock(new Block().setTitle("Brick").setId((byte) 20).setTextureAtlasPos(new Vector2f(7, 0)))
// <--

// Billboards
bm.addBlock(new Block().setTitle("Red flower").setId((byte) 16).setTextureAtlasPos(new Vector2f(13, 0)).setTranslucent(true).setPenetrable(true).setTranslucent(true).setColorSource(COLOR_SOURCE.FOLIAGE_LUT).setBlockForm(BLOCK_FORM.BILLBOARD))

bm.addBlock(new Block().setTitle("Yellow flower").setId((byte) 17).setTextureAtlasPos(new Vector2f(12, 0)).setTranslucent(true).setPenetrable(true).setColorSource(COLOR_SOURCE.FOLIAGE_LUT).setBlockForm(BLOCK_FORM.BILLBOARD).setAllowBlockAttachment(false))

bm.addBlock(new Block().setTitle("High grass").setId((byte) 13).setTextureAtlasPos(new Vector2f(12, 11)).setTranslucent(true).setPenetrable(true).setColorSource(COLOR_SOURCE.FOLIAGE_LUT).setBlockForm(BLOCK_FORM.BILLBOARD).setAllowBlockAttachment(false))

bm.addBlock(new Block().setTitle("Medium high grass").setId((byte) 14).setTextureAtlasPos(new Vector2f(13, 11)).setTranslucent(true).setPenetrable(true).setColorSource(COLOR_SOURCE.FOLIAGE_LUT).setBlockForm(BLOCK_FORM.BILLBOARD).setAllowBlockAttachment(false))

bm.addBlock(new Block().setTitle("Large high grass").setId((byte) 15).setTextureAtlasPos(new Vector2f(14, 11)).setTranslucent(true).setPenetrable(true).setColorSource(COLOR_SOURCE.FOLIAGE_LUT).setBlockForm(BLOCK_FORM.BILLBOARD).setAllowBlockAttachment(false))
// <--

// Cacti
bm.addBlock(new Block().setTitle("Cactus").setId((byte) 28).setTextureAtlasPosTopBottom(new Vector2f(5, 4)).setTextureAtlasPosMantle(new Vector2f(6, 4)).setBlockForm(BLOCK_FORM.CACTUS).setDisableTesselation(true).setTranslucent(true))
// <--

// Minerals
bm.addBlock(new Block().setTitle("Coal").setId((byte) 21).setTextureAtlasPos(new Vector2f(2, 2)))

bm.addBlock(new Block().setTitle("Diamond").setId((byte) 25).setTextureAtlasPos(new Vector2f(2, 3)))

bm.addBlock(new Block().setTitle("Gold").setId((byte) 24).setTextureAtlasPos(new Vector2f(0, 2)))

bm.addBlock(new Block().setTitle("Red stone").setId((byte) 23).setTextureAtlasPos(new Vector2f(3, 3)))

bm.addBlock(new Block().setTitle("Silver").setId((byte) 22).setTextureAtlasPos(new Vector2f(1, 2)))
// <--

// Light sources
bm.addBlock(new Block().setTitle("Torch").setId((byte) 29).setTextureAtlasPos(new Vector2f(0, 5)).setTranslucent(true).setPenetrable(true).setBlockForm(BLOCK_FORM.BILLBOARD).setLuminance((byte) 15).setAllowBlockAttachment(false))
// <--

// Leafs
bm.addBlock(new Block().setTitle("Leaf").setId((byte) 11).setTextureAtlasPos(new Vector2f(4, 3)).setTranslucent(true).setDisableTesselation(true).setAllowBlockAttachment(false).setColorSource(COLOR_SOURCE.FOLIAGE_LUT))
bm.addBlock(new Block().setTitle("Red leaf").setId((byte) 30).setTextureAtlasPos(new Vector2f(4, 3)).setTranslucent(true).setDisableTesselation(true).setAllowBlockAttachment(false).setColorSource(COLOR_SOURCE.FOLIAGE_LUT).setColorOffset(new Vector4f(1.0f, 0.8f, 0.8f, 1.0f)))
bm.addBlock(new Block().setTitle("Dark leaf").setId((byte) 12).setTextureAtlasPos(new Vector2f(4, 3)).setTranslucent(true).setDisableTesselation(true).setAllowBlockAttachment(false).setColorSource(COLOR_SOURCE.FOLIAGE_LUT).setColorOffset(new Vector4f(0.5f, 0.5f, 0.5f, 1.0f)))
// <--