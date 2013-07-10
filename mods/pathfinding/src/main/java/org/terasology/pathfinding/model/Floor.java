package org.terasology.pathfinding.model;

import com.google.common.collect.Lists;
import org.terasology.math.TeraMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author synopia
 */
public class Floor extends BaseRegion<Floor> {
    public HeightMap heightMap;
    private Entrance[] entranceMap;
    private List<Entrance> entrances;

    public Floor(HeightMap heightMap, int id) {
        super(id);
        this.heightMap = heightMap;
        entranceMap = new Entrance[HeightMap.SIZE_X*HeightMap.SIZE_Z];
        entrances = Lists.newArrayList();
    }

    public boolean overlap( Region region ) {
        return map.overlap(region.map);
    }

    public void addNeighborBlock(WalkableBlock neighbor) {
        neighborRegions.add(neighbor.floor);
    }

    public void removeNeighborBlock(WalkableBlock neighbor) {
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

    public void resetEntrances() {
        Arrays.fill(entranceMap, null);
        entrances.clear();
    }

    public boolean isEntrance(WalkableBlock block) {
        return isEntrance(TeraMath.calcBlockPosX(block.x()), TeraMath.calcBlockPosZ(block.z()));
    }
    public boolean isEntrance(int x, int y) {
        return entranceMap[x+y*HeightMap.SIZE_Z]!=null;
    }
    public Entrance setEntrance(int x, int y) {
        if( entranceMap[x+y*HeightMap.SIZE_Z]!=null) {
            return entranceMap[x+y*HeightMap.SIZE_Z];
        }
        Entrance left = null;
        Entrance up = null;
        Entrance leftUp = null;
        if( x>0 ) {
            left = entranceMap[x - 1 + y * HeightMap.SIZE_Z];
        }
        if( y>0 ) {
            up = entranceMap[x+(y-1)*HeightMap.SIZE_Z];
        }
        if( x>0 && y>0 ) {
            leftUp = entranceMap[x-1+(y-1)*HeightMap.SIZE_Z];
        }
        Entrance entrance;
        if( left==null && up==null && leftUp==null ) {
            entrance = new Entrance(this);
            entrance.addToEntrance(x, y);
            entrances.add(entrance);
        } else {
            entrance = left!=null ? left : up!=null ? up : leftUp;
            if( entrance.isPartOfEntrance(x,y)) {
                entrance.addToEntrance(x,y);
            } else {
                entrance = new Entrance(this);
                entrance.addToEntrance(x, y);
                entrances.add(entrance);
            }
        }
        entranceMap[x+y*HeightMap.SIZE_Z] = entrance;
        return entrance;
    }
    public void setEntrance(WalkableBlock block, WalkableBlock neighbor) {
        Entrance entrance = setEntrance(TeraMath.calcBlockPosX(block.x()), TeraMath.calcBlockPosZ(block.z()));
        entrance.neighborFloors.add( neighbor.floor );
    }

    public List<Entrance> entrances() {
        return entrances;
    }

    WalkableBlock getBlock( int fx, int fy ) {
        HeightMapCell cell = heightMap.getCell(fx, fy);
        for (WalkableBlock block : cell.blocks) {
            if( block.floor==this ) {
                return block;
            }
        }
        return null;
    }
}
