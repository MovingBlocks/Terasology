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
    private String _title;
    private BlockMeshPart _centerMesh;
    private EnumMap<Side, BlockMeshPart> _meshParts = new EnumMap<Side, BlockMeshPart>(Side.class);
    private boolean[] _fullSide = new boolean[Side.values().length];
    private List<AABB> _colliders = new ArrayList<AABB>();

    public BlockShape(String title) {
        _title = title;
        for (int i = 0; i < _fullSide.length; ++i) {
            _fullSide[i] = false;
        }
    }

    public BlockMeshPart getCenterMesh() {
        return _centerMesh;
    }

    public BlockMeshPart getSideMesh(Side side) {
        return _meshParts.get(side);
    }

    public boolean isBlockingSide(Side side) {
        return _fullSide[side.ordinal()];
    }

    public String getTitle() {
        return _title;
    }
    
    public Iterable<AABB> getColliders() {
        return _colliders;
    }

    public void setCenterMesh(BlockMeshPart mesh) {
        _centerMesh = mesh;
    }

    public void setSideMesh(Side side, BlockMeshPart mesh) {
        _meshParts.put(side, mesh);
    }

    public void setBlockingSide(Side side, boolean blocking) {
        _fullSide[side.ordinal()] = blocking;
    }
    
    public void setColliders(Collection<AABB> colliders) {
        _colliders.clear();
        _colliders.addAll(colliders);
    }

}
