package org.terasology.model.shapes;

import com.bulletphysics.collision.shapes.CollisionShape;
import com.google.common.collect.Maps;
import org.terasology.math.Rotation;
import org.terasology.math.Side;

import javax.vecmath.Vector3f;
import java.util.EnumMap;

/**
 * Describes a shape that a block can take. The shape may also be rotated if not symmetrical.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class BlockShape {
    private String title;
    private BlockMeshPart centerMesh;
    private EnumMap<Side, BlockMeshPart> meshParts = new EnumMap<Side, BlockMeshPart>(Side.class);
    private boolean[] fullSide = new boolean[Side.values().length];
    private EnumMap<Rotation, CollisionShape> collisionShape = Maps.newEnumMap(Rotation.class);
    private EnumMap<Rotation, Vector3f> collisionOffset = Maps.newEnumMap(Rotation.class);
    private boolean collisionSymmetric = false;

    public BlockShape(String title) {
        this.title = title;
        for (Rotation rot : Rotation.horizontalRotations()) {
            collisionOffset.put(rot, new Vector3f());
        }
        for (int i = 0; i < fullSide.length; ++i) {
            fullSide[i] = false;
        }
    }

    public BlockMeshPart getCenterMesh() {
        return centerMesh;
    }

    public BlockMeshPart getSideMesh(Side side) {
        return meshParts.get(side);
    }

    public boolean isBlockingSide(Side side) {
        return fullSide[side.ordinal()];
    }

    public String getTitle() {
        return title;
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

    public void setCenterMesh(BlockMeshPart mesh) {
        centerMesh = mesh;
    }

    public void setSideMesh(Side side, BlockMeshPart mesh) {
        meshParts.put(side, mesh);
    }

    public void setBlockingSide(Side side, boolean blocking) {
        fullSide[side.ordinal()] = blocking;
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
