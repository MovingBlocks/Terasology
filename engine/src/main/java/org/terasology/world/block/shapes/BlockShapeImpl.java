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

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.google.common.collect.Maps;
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
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
 * @author Immortius
 */
public class BlockShapeImpl extends AbstractAsset<BlockShapeData> implements BlockShape {

    private String displayName;
    private EnumMap<BlockPart, BlockMeshPart> meshParts = Maps.newEnumMap(BlockPart.class);
    private EnumBooleanMap<Side> fullSide = new EnumBooleanMap<>(Side.class);
    private CollisionShape baseCollisionShape;
    private Vector3f baseCollisionOffset = new Vector3f();
    private boolean yawSymmetric;
    private boolean pitchSymmetric;
    private boolean rollSymmetric;

    private Map<Rotation, CollisionShape> collisionShape = Maps.newHashMap();

    public BlockShapeImpl(AssetUri uri, BlockShapeData data) {
        super(uri);
        reload(data);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public BlockMeshPart getMeshPart(BlockPart part) {
        return meshParts.get(part);
    }

    public boolean isBlockingSide(Side side) {
        return fullSide.get(side);
    }

    @Override
    public void reload(BlockShapeData data) {
        collisionShape.clear();
        displayName = data.getDisplayName();
        for (BlockPart part : BlockPart.values()) {
            this.meshParts.put(part, data.getMeshPart(part));
        }
        for (Side side : Side.values()) {
            this.fullSide.put(side, data.isBlockingSide(side));
        }
        this.baseCollisionShape = data.getCollisionShape();
        this.baseCollisionOffset.set(data.getCollisionOffset());
        collisionShape.put(Rotation.none(), baseCollisionShape);

        yawSymmetric = data.isYawSymmetric();
        pitchSymmetric = data.isPitchSymmetric();
        rollSymmetric = data.isRollSymmetric();
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    public CollisionShape getCollisionShape(Rotation rot) {
        Rotation simplifiedRot = applySymmetry(rot);
        CollisionShape result = collisionShape.get(simplifiedRot);
        if (result == null && baseCollisionShape != null) {
            result = rotate(baseCollisionShape, simplifiedRot.getQuat4f());
            collisionShape.put(simplifiedRot, result);
        }
        return result;
    }

    public Vector3f getCollisionOffset(Rotation rot) {
        Rotation simplifiedRot = applySymmetry(rot);
        if (simplifiedRot.equals(Rotation.none())) {
            return new Vector3f(baseCollisionOffset);
        }
        return QuaternionUtil.quatRotate(simplifiedRot.getQuat4f(), baseCollisionOffset, new Vector3f());
    }

    @Override
    public boolean isCollisionYawSymmetric() {
        return yawSymmetric;
    }

    private Rotation applySymmetry(Rotation rot) {
        return Rotation.rotate(yawSymmetric ? Yaw.NONE : rot.getYaw(), pitchSymmetric ? Pitch.NONE : rot.getPitch(), rollSymmetric ? Roll.NONE : rot.getRoll());
    }

    private CollisionShape rotate(CollisionShape shape, Quat4f rot) {
        if (shape instanceof BoxShape) {
            BoxShape box = (BoxShape) shape;
            Vector3f extents = box.getHalfExtentsWithMargin(new Vector3f());
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

