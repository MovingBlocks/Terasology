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


import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3Array;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3FloatData;
import com.google.common.collect.Maps;
import org.lwjgl.BufferUtils;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.*;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.utilities.collection.EnumBooleanMap;
import org.terasology.world.block.BlockPart;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.EnumMap;
import java.util.Map;

/**
 */
public class BlockShapeImpl extends BlockShape {

    private String displayName;
    private EnumMap<BlockPart, BlockMeshPart> meshParts = Maps.newEnumMap(BlockPart.class);
    private EnumBooleanMap<Side> fullSide = new EnumBooleanMap<>(Side.class);
    private btCollisionShape baseCollisionShape;
    private Vector3f baseCollisionOffset = new Vector3f();
    private boolean yawSymmetric;
    private boolean pitchSymmetric;
    private boolean rollSymmetric;

    private Map<Rotation, btCollisionShape> collisionShape = Maps.newHashMap();

    public BlockShapeImpl(ResourceUrn urn, AssetType<?, BlockShapeData> assetType, BlockShapeData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public BlockMeshPart getMeshPart(BlockPart part) {
        return meshParts.get(part);
    }

    @Override
    public boolean isBlockingSide(Side side) {
        return fullSide.get(side);
    }

    @Override
    protected void doReload(BlockShapeData data) {
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
    public btCollisionShape getCollisionShape(Rotation rot) {
        Rotation simplifiedRot = applySymmetry(rot);
        btCollisionShape result = collisionShape.get(simplifiedRot);
        if (result == null && baseCollisionShape != null) {
            result = rotate(baseCollisionShape, simplifiedRot.getQuat4f());
            collisionShape.put(simplifiedRot, result);
        }
        return result;
    }

    @Override
    public Vector3f getCollisionOffset(Rotation rot) {
        Rotation simplifiedRot = applySymmetry(rot);
        if (simplifiedRot.equals(Rotation.none())) {
            return new Vector3f(baseCollisionOffset);
        }
        return simplifiedRot.getQuat4f().rotate(baseCollisionOffset, new Vector3f());
    }

    @Override
    public boolean isCollisionYawSymmetric() {
        return yawSymmetric;
    }

    private Rotation applySymmetry(Rotation rot) {
        return Rotation.rotate(yawSymmetric ? Yaw.NONE : rot.getYaw(), pitchSymmetric ? Pitch.NONE : rot.getPitch(), rollSymmetric ? Roll.NONE : rot.getRoll());
    }

    private btCollisionShape rotate(btCollisionShape shape, Quat4f rot) {

        Matrix4 transform = new Matrix4(new Vector3(),VecMath.to(rot),new Vector3(1,1,1));

        if (shape instanceof btBoxShape) {
            btBoxShape box = (btBoxShape) shape;
            Vector3 halfExtentsWithMargin = new Vector3(box.getHalfExtentsWithMargin());

            VecMath.to(rot).transform(halfExtentsWithMargin);
            halfExtentsWithMargin.x = Math.abs(halfExtentsWithMargin.x);
            halfExtentsWithMargin.y = Math.abs(halfExtentsWithMargin.y);
            halfExtentsWithMargin.z = Math.abs(halfExtentsWithMargin.z);

            return new btBoxShape(halfExtentsWithMargin);
        } else if (shape instanceof btCompoundShape) {
            btCompoundShape compound = (btCompoundShape) shape;
            btCompoundShape newShape = new btCompoundShape();

            btCompoundShapeChild childList = compound.getChildList();
            for(int i = 0; i < compound.getNumChildShapes(); i++)
            {
                btCollisionShape rotatedChild =  rotate(compound.getChildShape(i),rot);
                newShape.addChildShape(new Matrix4(VecMath.to(Rotation.none().getQuat4f())),rotatedChild);
            }
            return newShape;
        } else if (shape instanceof btConvexHullShape) {
            btConvexHullShape convexHull = (btConvexHullShape) shape;

            FloatBuffer buffer = BufferUtils.createFloatBuffer(convexHull.getNumPoints() * 3);
            for (int i = 0; i < convexHull.getNumPoints(); i++){
                Vector3 vertex = convexHull.getScaledPoint(i).mul(transform);
                buffer.put(vertex.x);
                buffer.put(vertex.y);
                buffer.put(vertex.z);
            }

            return new btConvexHullShape(buffer,convexHull.getNumPoints(),3 * Float.BYTES);
        }
        return shape;
    }
}

