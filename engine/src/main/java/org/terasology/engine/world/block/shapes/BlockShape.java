// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.shapes;

import org.joml.Vector3f;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.Side;
import org.terasology.engine.physics.shapes.CollisionShape;
import org.terasology.engine.world.block.BlockPart;

/**
 * Describes a shape that a block can take. The shape may also be rotated if not symmetrical.
 *
 */
public abstract class BlockShape extends Asset<BlockShapeData> {

    public BlockShape(ResourceUrn urn, AssetType<?, BlockShapeData> assetType) {
        super(urn, assetType);
    }

    /**
     * @return The display name for the shape - used as a suffix for blocks automatically generated using the shape.
     */
    public abstract String getDisplayName();

    /**
     * @param part
     * @return The mesh part for the given part of the block, or null if it has none
     */
    public abstract BlockMeshPart getMeshPart(BlockPart part);

    /**
     * @param side
     * @return Whether the given side blocks
     */
    public abstract boolean isBlockingSide(Side side);

    /**
     * @param rot
     * @return The collision shape for the given rotation
     */
    public abstract CollisionShape getCollisionShape(Rotation rot);

    /**
     * @param rot
     * @return The collision offset for the given rotation
     */
    public abstract Vector3f getCollisionOffset(Rotation rot);


    /**
     * @return Is this block shape's collision symmetric when altering yaw.
     */
    public abstract boolean isCollisionYawSymmetric();
}
