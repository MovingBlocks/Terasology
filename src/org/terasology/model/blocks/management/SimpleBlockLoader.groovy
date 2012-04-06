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
import org.terasology.math.Rotation

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
        return loadBlock(blockConfig, Rotation.None);
    }
    
    public Block loadBlock(ConfigObject blockConfig, Rotation rotation) {
        Block block = new Block();
        configureBlock(block, blockConfig, rotation);
        return block;
    }

    protected void configureBlock(Block b, ConfigObject c, Rotation rotation)
    {
         // *** BLOCK_FORM and COLOR_SOURCE enums (defined explicitly in block definition, not needed here)
        if (c.blockform != [:]) {
            log.fine "Setting BLOCK_FORM enum to: " + c.blockform
            b.withBlockForm(c.blockform)
        }
        if (c.colorsource != [:]) {
            log.fine "Setting COLOR_SOURCE enum to: " + c.colorsource
            b.withColorSource(c.colorsource)
        }
        log.fine "Block has form " + b.getBlockForm() + ", and color source " + b.getColorSource()

        // *** BOOLEANS - IntelliJ may warn about "null" about here but it works alright
        // Casting to (boolean) removes the warning but is functionally unnecessary
        if (c.translucent != [:]) {
            log.fine "Setting translucent boolean to: " + c.translucent
            b.withTranslucent((boolean) c.translucent)
        }
        if (c.invisible != [:]) {
            log.fine "Setting invisible boolean to: " + c.invisible
            b.withInvisible((boolean) c.invisible)
        }
        if (c.waving != [:]) {
            log.fine "Setting waving boolean to: " + c.waving
            b.withWaving((boolean) c.waving)
        }
        if (c.penetrable != [:]) {
            log.fine "Setting penetrable boolean to: " + c.penetrable
            b.withPenetrable((boolean) c.penetrable)
        }
        if (c.castsShadows != [:]) {
            log.fine "Setting castsShadows boolean to: " + c.castsShadows
            b.withCastsShadows((boolean) c.castsShadows)
        }
        if (c.renderBoundingBox != [:]) {
            log.fine "Setting renderBoundingBox boolean to: " + c.renderBoundingBox
            b.withRenderBoundingBox((boolean) c.renderBoundingBox)
        }
        if (c.allowBlockAttachment != [:]) {
            log.fine "Setting allowBlockAttachment boolean to: " + c.allowBlockAttachment
            b.withAllowBlockAttachment((boolean) c.allowBlockAttachment)
        }
        if (c.bypassSelectionRay != [:]) {
            log.fine "Setting bypassSelectionRay boolean to: " + c.bypassSelectionRay
            b.withBypassSelectionRay((boolean) c.bypassSelectionRay)
        }

        // *** PHYSICS
        if (c.physics.mass != [:]) {
            log.fine "Setting mass to: " + c.physics.mass
            b.withMass((float) c.physics.mass)
        }

        // *** MISC
        if (c.lootAmount != [:]) {
            log.fine "Setting loot amount to: " + c.lootAmount
            b.withLootAmount((byte) c.lootAmount)
        }

        if (c.luminance != [:]) {
            log.fine "Setting luminance to: " + c.luminance
            b.withLuminance((byte) c.luminance)
        }
        if (c.hardness != [:]) {
            log.fine "Setting hardness to: " + c.hardness
            b.withHardness((byte) c.hardness)
        }
        if (c.straightToInventory != [:]) {
            log.fine "Setting straightToInventory to: " + c.straightToInventory
            b.withStraightToInventory(c.straightToInventory)
        }
        if (c.stackable != [:]) {
            log.fine "Setting stackable to: " + c.stackable
            b.withStackable(c.stackable)
        }
        if (c.entityRetainedWhenItem != [:]) {
            log.fine "Setting entityRetainedWhenItem to: " + c.entityRetainedWhenItem
            b.withEntityRetainedWhenItem(c.entityRetainedWhenItem)
        }
        if (c.usable != [:]) {
            log.fine "Setting usable to: " + c.usable
            b.withUsable(c.usable)
        }

        // *** COLOR OFFSET (4 values) - this might need error handling
        if (c.colorOffset != [:]) {
            log.fine "Setting colorOffset to: " + c.colorOffset + " (after making it a Vector4f)"
            b.withColorOffset(new Vector4f((float) c.colorOffset[0], (float) c.colorOffset[1], (float) c.colorOffset[2], (float) c.colorOffset[3]))
            log.fine "The Vector4f instantiated is" + b.getColorOffset()
        }
    }

}
