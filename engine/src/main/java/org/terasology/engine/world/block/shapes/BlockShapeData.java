// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.shapes;

import com.google.common.collect.Maps;
import org.joml.Vector3f;
import org.terasology.engine.math.Side;
import org.terasology.engine.physics.shapes.CollisionShape;
import org.terasology.engine.world.block.BlockPart;
import org.terasology.gestalt.assets.AssetData;

import java.util.EnumMap;

/**
 */
public class BlockShapeData implements AssetData {
    private String displayName = "";
    private EnumMap<BlockPart, BlockMeshPart> meshParts = Maps.newEnumMap(BlockPart.class);
    private boolean[] fullSide = new boolean[Side.values().length];
    private CollisionShape collisionShape;
    private Vector3f collisionOffset = new Vector3f();
    private boolean yawSymmetric;
    private boolean pitchSymmetric;
    private boolean rollSymmetric;

    public String getDisplayName() {
        if (displayName == null) {
            return "";
        }
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BlockMeshPart getMeshPart(BlockPart part) {
        return meshParts.get(part);
    }

    /**
     * Sets the mesh to use for the given block part
     *
     * @param part
     * @param mesh
     */
    public void setMeshPart(BlockPart part, BlockMeshPart mesh) {
        meshParts.put(part, mesh);
    }

    public boolean isBlockingSide(Side side) {
        return fullSide[side.ordinal()];
    }

    /**
     * Sets whether the given side blocks the view of adjacent tiles (that is, it fills the side)
     *
     * @param side
     * @param blocking
     */
    public void setBlockingSide(Side side, boolean blocking) {
        fullSide[side.ordinal()] = blocking;
    }

    public Vector3f getCollisionOffset() {
        return collisionOffset;
    }

    public void setCollisionOffset(Vector3f offset) {
        collisionOffset.set(offset);
    }

    public CollisionShape getCollisionShape() {
        return collisionShape;
    }

    public void setCollisionShape(CollisionShape shape) {
        collisionShape = shape;
    }

    public void setCollisionSymmetric(boolean collisionSymmetric) {
        yawSymmetric = collisionSymmetric;
        pitchSymmetric = collisionSymmetric;
        rollSymmetric = collisionSymmetric;
    }

    public boolean isRollSymmetric() {
        return rollSymmetric;
    }

    public boolean isPitchSymmetric() {
        return pitchSymmetric;
    }

    public boolean isYawSymmetric() {
        return yawSymmetric;
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
}
