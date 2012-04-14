package org.terasology.model.blocks.management

import groovy.util.logging.Log
import org.terasology.math.Rotation
import org.terasology.model.blocks.Block

import javax.vecmath.Vector4f

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
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting BLOCK_FORM enum to: " + c.blockform
            b.withBlockForm(c.blockform)
        }
        if (c.colorsource != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting COLOR_SOURCE enum to: " + c.colorsource
            b.withColorSource(c.colorsource)
        }
        org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Block has form " + b.getBlockForm() + ", and color source " + b.getColorSource()

        // *** BOOLEANS - IntelliJ may warn about "null" about here but it works alright
        // Casting to (boolean) removes the warning but is functionally unnecessary
        if (c.translucent != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting translucent boolean to: " + c.translucent
            b.withTranslucent((boolean) c.translucent)
        }
        if (c.invisible != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting invisible boolean to: " + c.invisible
            b.withInvisible((boolean) c.invisible)
        }
        if (c.waving != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting waving boolean to: " + c.waving
            b.withWaving((boolean) c.waving)
        }
        if (c.penetrable != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting penetrable boolean to: " + c.penetrable
            b.withPenetrable((boolean) c.penetrable)
        }
        if (c.castsShadows != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting castsShadows boolean to: " + c.castsShadows
            b.withCastsShadows((boolean) c.castsShadows)
        }
        if (c.renderBoundingBox != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting renderBoundingBox boolean to: " + c.renderBoundingBox
            b.withRenderBoundingBox((boolean) c.renderBoundingBox)
        }
        if (c.allowBlockAttachment != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting allowBlockAttachment boolean to: " + c.allowBlockAttachment
            b.withAllowBlockAttachment((boolean) c.allowBlockAttachment)
        }
        if (c.bypassSelectionRay != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting bypassSelectionRay boolean to: " + c.bypassSelectionRay
            b.withBypassSelectionRay((boolean) c.bypassSelectionRay)
        }

        // *** PHYSICS
        if (c.physics.mass != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting mass to: " + c.physics.mass
            b.withMass((float) c.physics.mass)
        }

        // *** MISC
        if (c.lootAmount != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting loot amount to: " + c.lootAmount
            b.withLootAmount((byte) c.lootAmount)
        }

        if (c.luminance != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting luminance to: " + c.luminance
            b.withLuminance((byte) c.luminance)
        }
        if (c.hardness != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting hardness to: " + c.hardness
            b.withHardness((byte) c.hardness)
        }
        if (c.straightToInventory != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting straightToInventory to: " + c.straightToInventory
            b.withStraightToInventory(c.straightToInventory)
        }
        if (c.stackable != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting stackable to: " + c.stackable
            b.withStackable(c.stackable)
        }
        if (c.entityRetainedWhenItem != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting entityRetainedWhenItem to: " + c.entityRetainedWhenItem
            b.withEntityRetainedWhenItem(c.entityRetainedWhenItem)
        }
        if (c.usable != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting usable to: " + c.usable
            b.withUsable(c.usable)
        }
        if (c.entityPrefab != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting entityPrefab to: " + c.entityPrefab
            b.withEntityPrefab(c.entityPrefab)
        }

        // *** COLOR OFFSET (4 values) - this might need error handling
        if (c.colorOffset != [:]) {
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "Setting colorOffset to: " + c.colorOffset + " (after making it a Vector4f)"
            b.withColorOffset(new Vector4f((float) c.colorOffset[0], (float) c.colorOffset[1], (float) c.colorOffset[2], (float) c.colorOffset[3]))
            org.terasology.model.blocks.management.SimpleBlockLoader.log.fine "The Vector4f instantiated is" + b.getColorOffset()
        }
    }

}
