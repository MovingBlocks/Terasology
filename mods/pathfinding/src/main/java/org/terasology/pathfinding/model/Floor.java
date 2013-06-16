package org.terasology.pathfinding.model;

import java.util.*;

/**
 * @author synopia
 */
public class Floor extends BaseRegion<Floor> {
    public HeightMap heightMap;

    public Floor(HeightMap heightMap, int id) {
        super(id);
        this.heightMap = heightMap;
    }

    public boolean overlap( Region region ) {
        return map.overlap(region.map);
    }

    public void addNeighborBlock( WalkableBlock current, WalkableBlock neighbor ) {
        neighborRegions.add(neighbor.floor );
        setContour(current);
        neighbor.floor.setContour(neighbor);
    }

    public void removeNeighborBlock( WalkableBlock current, WalkableBlock neighbor ) {
        neighborRegions.remove(neighbor.floor);
    }

    public void merge( Region neighbor ) {
        map.merge(neighbor.map);
        neighbor.floor = this;
        for (Region neighborRegion : neighbor.neighborRegions) {
            if( neighborRegion.floor!=null && neighborRegion.floor!=this ) {
                neighborRegions.add(neighborRegion.floor);
                neighborRegion.floor.neighborRegions.add(this);
            }
        }
    }

    public Iterable<WalkableBlock> contour() {
        return new Iterable<WalkableBlock>() {
            @Override
            public Iterator<WalkableBlock> iterator() {
                return new Iterator<WalkableBlock>() {
                    int pos = 0;
                    @Override
                    public boolean hasNext() {
                        return contour.map.nextSetBit(pos)!=-1;
                    }

                    @Override
                    public WalkableBlock next() {
                        int index = contour.map.nextSetBit(pos);
                        pos = index+1;
                        int x = index%HeightMap.SIZE_X;
                        int z = index/HeightMap.SIZE_X;
                        HeightMapCell cell = heightMap.getCell(x, z);
                        for (WalkableBlock block : cell.blocks) {
                            if( block.floor==Floor.this ) {
                                return block;
                            }
                        }
                        return null;
                    }

                    @Override
                    public void remove() {
                    }
                };
            }
        };
    }

}
