/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
import com.google.common.collect.Maps;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;
import org.terasology.collection.EnumBooleanMap;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.world.block.BlockPart;

import javax.vecmath.Vector3f;
import java.util.EnumMap;

/**
 * Describes a shape that a block can take. The shape may also be rotated if not symmetrical.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class BlockShape implements Asset {
    private AssetUri uri;
    private EnumMap<BlockPart, BlockMeshPart> meshParts = Maps.newEnumMap(BlockPart.class);
    private EnumBooleanMap<Side> fullSide = new EnumBooleanMap<Side>(Side.class);
    private EnumMap<Rotation, CollisionShape> collisionShape = Maps.newEnumMap(Rotation.class);
    private EnumMap<Rotation, Vector3f> collisionOffset = Maps.newEnumMap(Rotation.class);
    private boolean collisionSymmetric = false;

    public BlockShape() {
        for (Rotation rot : Rotation.horizontalRotations()) {
            collisionOffset.put(rot, new Vector3f());
        }
    }

    public BlockMeshPart getMeshPart(BlockPart part) {
        return meshParts.get(part);
    }

    public boolean isBlockingSide(Side side) {
        return fullSide.get(side);
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

    @Override
    public void dispose() {
    }

    public void setURI(AssetUri uri) {
        this.uri = uri;
    }

    public CollisionShape getCollisionShape() {
        return collisionShape.get(Rotation.NONE);
    }

    public Vector3f getCollisionOffset() {
        return collisionOffset.get(Rotation.NONE);
    }

    public CollisionShape getCollisionShape(Rotation rot) {
        if (isCollisionSymmetric()) {
            return collisionShape.get(Rotation.NONE);
        }
        return collisionShape.get(rot);
    }

    public Vector3f getCollisionOffset(Rotation rot) {
        if (isCollisionSymmetric()) {
            return collisionOffset.get(Rotation.NONE);
        }
        return collisionOffset.get(rot);
    }

    public void setMeshPart(BlockPart part, BlockMeshPart mesh) {
        meshParts.put(part, mesh);
    }

    public void setBlockingSide(Side side, boolean blocking) {
        fullSide.put(side, blocking);
    }

    public void setCollisionOffset(Vector3f offset) {
        collisionOffset.get(Rotation.NONE).set(offset);
    }

    public void setCollisionShape(CollisionShape shape) {
        collisionShape.put(Rotation.NONE, shape);
    }

    public void setCollisionOffset(Rotation rot, Vector3f offset) {
        collisionOffset.get(rot).set(offset);
    }

    public void setCollisionShape(Rotation rot, CollisionShape shape) {
        collisionShape.put(rot, shape);
    }

    public boolean isCollisionSymmetric() {
        return collisionSymmetric;
    }

    public void setCollisionSymmetric(boolean collisionSymmetric) {
        this.collisionSymmetric = collisionSymmetric;
    }


}
