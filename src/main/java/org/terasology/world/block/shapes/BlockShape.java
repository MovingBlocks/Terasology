/*
 * Copyright 2013 Moving Blocks
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

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.google.common.collect.Maps;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetData;
import org.terasology.asset.AssetUri;
import org.terasology.asset.CompatibilityHackAsset;
import org.terasology.math.Pitch;
import org.terasology.math.Roll;
import org.terasology.math.Rotation;
import org.terasology.math.Side;
import org.terasology.math.Yaw;
import org.terasology.utilities.collection.EnumBooleanMap;
import org.terasology.world.block.BlockPart;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.EnumMap;
import java.util.Map;

/**
 * Describes a shape that a block can take. The shape may also be rotated if not symmetrical.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class BlockShape extends CompatibilityHackAsset implements Asset<AssetData> {
    private EnumMap<BlockPart, BlockMeshPart> meshParts = Maps.newEnumMap(BlockPart.class);
    private EnumBooleanMap<Side> fullSide = new EnumBooleanMap<Side>(Side.class);
    private CollisionShape baseCollisionShape;
    private Vector3f baseCollisionOffset = new Vector3f();
    private Map<Rotation, CollisionShape> collisionShape = Maps.newHashMap();
    private boolean yawSymmetric = false;
    private boolean pitchSymmetric = false;
    private boolean rollSymmetric = false;

    public BlockShape() {
    }

    public BlockMeshPart getMeshPart(BlockPart part) {
        return meshParts.get(part);
    }

    public boolean isBlockingSide(Side side) {
        return fullSide.get(side);
    }

    @Override
    public void dispose() {
    }

    public CollisionShape getCollisionShape() {
        return baseCollisionShape;
    }

    public Vector3f getCollisionOffset() {
        return baseCollisionOffset;
    }

    public CollisionShape getCollisionShape(Rotation rot) {
        if (isCollisionSymmetric()) {
            return baseCollisionShape;
        }
        rot = applySymmetry(rot);
        CollisionShape result = collisionShape.get(rot);
        if (result == null) {
            result = rotate(baseCollisionShape, rot.getQuat4f());
            collisionShape.put(rot, result);
        }
        return result;
    }

    public Vector3f getCollisionOffset(Rotation rot) {
        if (isCollisionSymmetric()) {
            return new Vector3f(baseCollisionOffset);
        }
        rot = applySymmetry(rot);
        return QuaternionUtil.quatRotate(rot.getQuat4f(), baseCollisionOffset, new Vector3f());
    }

    private Rotation applySymmetry(Rotation rot) {
        return Rotation.rotate(yawSymmetric ? Yaw.NONE : rot.getYaw(), pitchSymmetric ? Pitch.NONE : rot.getPitch(), rollSymmetric ? Roll.NONE : rot.getRoll());
    }

    public void setMeshPart(BlockPart part, BlockMeshPart mesh) {
        meshParts.put(part, mesh);
    }

    public void setBlockingSide(Side side, boolean blocking) {
        fullSide.put(side, blocking);
    }

    public void setCollisionOffset(Vector3f offset) {
        baseCollisionOffset.set(offset);
    }

    public void setCollisionShape(CollisionShape shape) {
        baseCollisionShape = shape;
        collisionShape.clear();
        collisionShape.put(Rotation.none(), shape);
    }

    public boolean isCollisionSymmetric() {
        return yawSymmetric && pitchSymmetric && rollSymmetric;
    }

    public void setCollisionSymmetric(boolean collisionSymmetric) {
        yawSymmetric = collisionSymmetric;
        pitchSymmetric = collisionSymmetric;
        rollSymmetric = collisionSymmetric;
    }

    public boolean isYawSymmetric() {
        return yawSymmetric;
    }

    public boolean isRollSymmetric() {
        return rollSymmetric;
    }

    public boolean isPitchSymmetric() {
        return pitchSymmetric;
    }

    public void setYawSymmetric(boolean yawSymmetric) {
        this.yawSymmetric = yawSymmetric;
    }

    public void setPitchSymmetric(boolean pitchSymmetric) {
        this.pitchSymmetric = pitchSymmetric;
    }

    public void setRollSymmetric(boolean rollSymmetric) {
        this.rollSymmetric = rollSymmetric;
    }

    private CollisionShape rotate(CollisionShape shape, Quat4f rot) {
        if (shape instanceof BoxShape) {
            BoxShape box = (BoxShape) shape;
            Vector3f extents = box.getHalfExtentsWithoutMargin(new Vector3f());
            QuaternionUtil.quatRotate(rot, extents, extents);
            extents.absolute();
            return new BoxShape(extents);
        } else if (shape instanceof CompoundShape) {
            CompoundShape compound = (CompoundShape) shape;
            CompoundShape newShape = new CompoundShape();
            for (CompoundShapeChild child : compound.getChildList()) {
                CollisionShape rotatedChild = rotate(child.childShape, rot);
                Vector3f offset = QuaternionUtil.quatRotate(rot, child.transform.origin, new Vector3f());
                newShape.addChildShape(new Transform(new Matrix4f(Rotation.none().getQuat4f(), offset, 1.0f)), rotatedChild);
            }
            return newShape;
        } else if (shape instanceof ConvexHullShape) {
            ConvexHullShape convexHull = (ConvexHullShape) shape;
            ObjectArrayList<Vector3f> transformedVerts = new ObjectArrayList<>();
            for (Vector3f vert : convexHull.getPoints()) {
                transformedVerts.add(QuaternionUtil.quatRotate(rot, vert, new Vector3f()));
            }
            return new ConvexHullShape(transformedVerts);
        }
        return shape;
    }


}
