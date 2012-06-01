package org.terasology.model.shapes;

import org.terasology.math.Side;
import org.terasology.model.structures.AABB;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

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
    private List<AABB> colliders = new ArrayList<AABB>();

    public BlockShape(String title) {
        this.title = title;
        for (int i = 0; i < fullSide.length; ++i)
            fullSide[i] = false;
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
    
    public Iterable<AABB> getColliders() {
        return colliders;
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
    
    public void setColliders(Collection<AABB> colliders) {
        this.colliders.clear();
        this.colliders.addAll(colliders);
    }

}
