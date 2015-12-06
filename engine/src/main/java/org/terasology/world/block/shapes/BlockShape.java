/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.block.shapes;

import com.bulletphysics.collision.shapes.CollisionShape;
import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.world.block.BlockPart;

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
