package org.terasology.model.blocks.management

import javax.vecmath.Vector2f
import org.terasology.math.Side
import org.terasology.model.shapes.BlockShape
import org.terasology.model.shapes.BlockShapeManager
import javax.vecmath.Vector4f
import groovy.util.logging.Log
import org.terasology.model.blocks.Block
import javax.vecmath.Vector3d
import org.terasology.model.structures.AABB

/**
 * @author Immortius <immortius@gmail.com>
 */
@Log
public class SimpleBlockLoader implements BlockLoader {

    private Map<String, Integer> _imageIndex;

    public SimpleBlockLoader(Map<String, Integer> imageIndex)
    {
        _imageIndex = imageIndex;
    }

    public Block loadBlock(ConfigObject blockConfig) {
        Block block = new Block();
        configureBlock(block, blockConfig);
        return block;
    }

    protected void configureBlock(Block b, ConfigObject c)
    {
        // Load Block details from Groovy, which may overwrite defaults from Block's Constructor
        println "Preparing block with name " + c.name

        // *** FACES - note how these are _not_ persisted in the Manifest, instead the texture name index values are
        // In theory this allows Blocks to change their faces without impacting the saved state of a world
        // First just set all 6 faces to the default for that block (its name for a png file)
        // This can return null if there's no default texture for a block, is ok if everything is set below
        // TODO: Might want to add some validation that all six sides have valid assignments at the end? Air gets all 0?
        println "Default image returns: " + _imageIndex.get(c.name)

        def textureId = _imageIndex.get(c.name)

        Vector2f centerTexturePos;

        if (textureId != null) {
            b.withTextureAtlasPos(calcAtlasPositionForId(textureId))
            centerTexturePos = calcAtlasPositionForId(textureId)
        }

        // Then look for each more specific assignment and overwrite defaults where needed
        if (c.block.faces.all != [:]) {
            println "Setting Block " + c.name + " to texture " + c.block.faces.all + " for all"
            b.withTextureAtlasPos(calcAtlasPositionForId(_imageIndex.get(c.block.faces.all)))
            centerTexturePos = calcAtlasPositionForId(_imageIndex.get(c.block.faces.all))
        }
        if (c.block.faces.center != [:])
            centerTexturePos = calcAtlasPositionForId(_imageIndex.get(c.block.faces.center))
        if (c.block.faces.sides != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.sides + " for sides"
            b.withTextureAtlasPosMantle(calcAtlasPositionForId(_imageIndex.get(c.block.faces.sides)))
        }
        if (c.block.faces.topbottom != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.topbottom + " for topbottom"
            b.withTextureAtlasPosTopBottom(calcAtlasPositionForId(_imageIndex.get(c.block.faces.topbottom)))
        }
        // Top, Bottom, Left, Right, Front, Back - probably a way to do that in a loop...
        if (c.block.faces.top != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.top + " for top"
            b.withTextureAtlasPos(Side.TOP, calcAtlasPositionForId(_imageIndex.get(c.block.faces.top)))
        }
        if (c.block.faces.bottom != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.bottom + " for bottom"
            b.withTextureAtlasPos(Side.BOTTOM, calcAtlasPositionForId(_imageIndex.get(c.block.faces.bottom)))
        }
        if (c.block.faces.left != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.left + " for left"
            b.withTextureAtlasPos(Side.LEFT, calcAtlasPositionForId(_imageIndex.get(c.block.faces.left)))
        }
        if (c.block.faces.right != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.right + " for right"
            b.withTextureAtlasPos(Side.RIGHT, calcAtlasPositionForId(_imageIndex.get(c.block.faces.right)))
        }
        if (c.block.faces.front != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.front + " for front"
            b.withTextureAtlasPos(Side.FRONT, calcAtlasPositionForId(_imageIndex.get(c.block.faces.front)))
        }
        if (c.block.faces.back != [:]) {
            println "Setting Block " + c.name + " to " + c.block.faces.back + " for back"
            b.withTextureAtlasPos(Side.BACK, calcAtlasPositionForId(_imageIndex.get(c.block.faces.back)))
        }
        println "Faces are (L, R, T, B, F, B): " + b.getTextureAtlasPos()

        // *** BLOCK_FORM and COLOR_SOURCE enums (defined explicitly in block definition, not needed here)
        if (c.block.blockform != [:]) {
            println "Setting BLOCK_FORM enum to: " + c.block.blockform
            b.withBlockForm(c.block.blockform)
        }
        if (c.block.colorsource != [:]) {
            println "Setting COLOR_SOURCE enum to: " + c.block.colorsource
            b.withColorSource(c.block.colorsource)
        }
        println "Block has form " + b.getBlockForm() + ", and color source " + b.getColorSource()

        // *** BOOLEANS - IntelliJ may warn about "null" about here but it works alright
        // Casting to (boolean) removes the warning but is functionally unnecessary
        if (c.block.translucent != [:]) {
            println "Setting translucent boolean to: " + c.block.translucent
            b.withTranslucent((boolean) c.block.translucent)
        }
        if (c.block.invisible != [:]) {
            println "Setting invisible boolean to: " + c.block.invisible
            b.withInvisible((boolean) c.block.invisible)
        }
        if (c.block.waving != [:]) {
            println "Setting waving boolean to: " + c.block.waving
            b.withWaving((boolean) c.block.waving)
        }
        if (c.block.penetrable != [:]) {
            println "Setting penetrable boolean to: " + c.block.penetrable
            b.withPenetrable((boolean) c.block.penetrable)
        }
        if (c.block.castsShadows != [:]) {
            println "Setting castsShadows boolean to: " + c.block.castsShadows
            b.withCastsShadows((boolean) c.block.castsShadows)
        }
        if (c.block.renderBoundingBox != [:]) {
            println "Setting renderBoundingBox boolean to: " + c.block.renderBoundingBox
            b.withRenderBoundingBox((boolean) c.block.renderBoundingBox)
        }
        if (c.block.allowBlockAttachment != [:]) {
            println "Setting allowBlockAttachment boolean to: " + c.block.allowBlockAttachment
            b.withAllowBlockAttachment((boolean) c.block.allowBlockAttachment)
        }
        if (c.block.bypassSelectionRay != [:]) {
            println "Setting bypassSelectionRay boolean to: " + c.block.bypassSelectionRay
            b.withBypassSelectionRay((boolean) c.block.bypassSelectionRay)
        }
        if (c.block.loweredShape != [:]) {
            BlockShape loweredShape;
            if (c.block.loweredShape != [:])
            {
                loweredShape = BlockShapeManager.getInstance().getBlockShape(c.block.loweredShape);
            }
            if (loweredShape != null)
            {
                println "Has lowered shape: " + c.block.loweredShape;
                for (Side side : Side.values())
                {
                    if (loweredShape.getSideMesh(side) != null)
                    {
                        b.withLoweredSideMesh(side, loweredShape.getSideMesh(side).mapTexCoords(b.calcTextureOffsetFor(side), Block.TEXTURE_OFFSET_WIDTH))
                    }
                }
            }
        }

        // *** PHYSICS
        if (c.block.physics.mass != [:]) {
            println "Setting mass to: " + c.block.physics.mass
            b.withMass((float) c.block.physics.mass)
        }

        // *** MISC
        if (c.block.lootAmount != [:]) {
            println "Setting loot amount to: " + c.block.lootAmount
            b.withLootAmount((byte) c.block.lootAmount)
        }

        if (c.block.luminance != [:]) {
            println "Setting luminance to: " + c.block.luminance
            b.withLuminance((byte) c.block.luminance)
        }
        if (c.block.hardness != [:]) {
            println "Setting hardness to: " + c.block.hardness
            b.withHardness((byte) c.block.hardness)
        }

        // *** COLOR OFFSET (4 values) - this might need error handling
        if (c.block.colorOffset != [:]) {
            println "Setting colorOffset to: " + c.block.colorOffset + " (after making it a Vector4f)"
            b.withColorOffset(new Vector4f((float) c.block.colorOffset[0], (float) c.block.colorOffset[1], (float) c.block.colorOffset[2], (float) c.block.colorOffset[3]))
            println "The Vector4f instantiated is" + b.getColorOffset()
        }


    }

    private Vector2f calcAtlasPositionForId(int id) {
        return new Vector2f(((int) id % (int) Block.ATLAS_ELEMENTS_PER_ROW_AND_COLUMN), ((int) id / (int) Block.ATLAS_ELEMENTS_PER_ROW_AND_COLUMN))
    }

}
