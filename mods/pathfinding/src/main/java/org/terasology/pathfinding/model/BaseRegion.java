package org.terasology.pathfinding.model;

import org.terasology.math.TeraMath;

import java.util.HashSet;
import java.util.Set;

/**
 * @author synopia
 */
public abstract class BaseRegion<N extends BaseRegion> {
    public int id;
    protected Set<N> neighborRegions = new HashSet<N>();
    protected BitMap map;
    protected Floor floor;

    protected BaseRegion( int id ) {
        this.id = id;
        map = new BitMap();
    }

    public void setPassable(int x, int y) {
        map.setPassable(x, y);
    }

    public BitMap getMap() {
        return map;
    }

    public Set<N> getNeighborRegions() {
        return neighborRegions;
    }

    @Override
    public String toString() {
        return id+"\nrc = " + neighborRegions.size();
    }
}
