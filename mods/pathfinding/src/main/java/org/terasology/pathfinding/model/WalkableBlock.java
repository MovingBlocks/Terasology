package org.terasology.pathfinding.model;

import org.terasology.math.Vector3i;

/**
* @author synopia
*/
public class WalkableBlock {
    private Vector3i position;
    public WalkableBlock[] neighbors = new WalkableBlock[8];
    public Floor floor;

    public WalkableBlock(int x, int z, int height) {
        position = new Vector3i(x, height, z);
    }

    public Vector3i getBlockPosition() {
        return position;
    }

    public int x() {
        return position.x;
    }
    public int z() {
        return position.z;
    }
    public int height() {
        return position.y;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(position.toString()).append("\n");
        if( floor!=null ) {
            sb.append(floor.toString()).append("\n");
        } else {
            sb.append("no floor\n");
        }
        return sb.toString();
    }

    public boolean hasNeighbor(WalkableBlock block) {
        for (WalkableBlock neighbor : neighbors) {
            if( neighbor==block ) {
                return true;
            }
        }
        return false;
    }
}
