package org.terasology.pathfinding.model;

import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.Chunk;

import java.util.*;

/**
* @author synopia
*/
public class HeightMap {
    public static final int SIZE_X = Chunk.SIZE_X;
    public static final int SIZE_Y = Chunk.SIZE_Y;
    public static final int SIZE_Z = Chunk.SIZE_Z;
    public static final int DIR_LEFT  = 0;
    public static final int DIR_LU    = 1;
    public static final int DIR_UP    = 2;
    public static final int DIR_RU    = 3;
    public static final int DIR_RIGHT = 4;
    public static final int DIR_RD    = 5;
    public static final int DIR_DOWN  = 6;
    public static final int DIR_LD    = 7;
    public static final int[][] DIRECTIONS = new int[][] {
            {-1,0}, {-1,-1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}
    };

    /* package protected */ HeightMapCell[] cells = new HeightMapCell[SIZE_X*SIZE_Z];
    public final List<WalkableBlock> walkableBlocks = new ArrayList<WalkableBlock>();
    public final List<Floor> floors = new ArrayList<Floor>();
    private WorldProvider world;
    public Vector3i worldPos;
    public final Set<WalkableBlock> borderBlocks = new HashSet<WalkableBlock>();
    public PathCache pathCache = new PathCache();

    public HeightMap(WorldProvider world, Vector3i chunkPos) {
        this.world = world;
        this.worldPos = new Vector3i(chunkPos);
        worldPos.mult(Chunk.SIZE_X, Chunk.SIZE_Y, Chunk.SIZE_Z);
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new HeightMapCell();
        }
    }

    public void update() {
        new WalkableBlockFinder(world).findWalkableBlocks(this);
        new FloorFinder(world).findFloors(this);
    }


    public void connectNeighborMaps( HeightMap left, HeightMap up, HeightMap right, HeightMap down ) {
        for (WalkableBlock block : borderBlocks) {
            int x = TeraMath.calcBlockPosX(block.getBlockPosition().x);
            int z = TeraMath.calcBlockPosZ(block.getBlockPosition().z);
            if( left!=null && x==0 ) {
                connectToNeighbor(block, HeightMap.SIZE_X-1, z, left, DIR_LEFT);
            }
            if( right!=null && x==HeightMap.SIZE_X-1 ) {
                connectToNeighbor(block, 0, z, right, DIR_RIGHT);
            }
            if( up!=null && z==0 ) {
                connectToNeighbor(block, x, HeightMap.SIZE_Z-1, up, DIR_UP);
            }
            if( down!=null && z==HeightMap.SIZE_Z-1 ) {
                connectToNeighbor(block, x, 0, down, DIR_DOWN);
            }
        }

        findContour();
        pathCache.clear();
        if( left!=null ) {
            left.findContour();
            left.pathCache.clear();
        }
        if( right!=null ) {
            right.findContour();
            right.pathCache.clear();
        }
        if( up!=null ) {
            up.findContour();
            up.pathCache.clear();
        }
        if( down!=null ) {
            down.findContour();
            down.pathCache.clear();
        }
    }

    public void findContour() {
        for (Floor floor : floors) {
            floor.resetContour();
        }
        for (int z = 0; z < HeightMap.SIZE_Z; z++) {
            for (int x = 0; x < HeightMap.SIZE_X; x++) {
                for (WalkableBlock block : getCell(x,z).blocks) {
                    Floor floor = block.floor;
                    boolean foundNeighborFloor = false;
                    for (WalkableBlock neighbor : block.neighbors) {
                        if( neighbor!=null && neighbor.floor!=floor ) {
                            int heightDiff = block.height()-neighbor.height();
                            if( heightDiff>0 ) {
                                if( world.getBlock(neighbor.x(),neighbor.height()+3, neighbor.z()).isPenetrable() ) {
                                    foundNeighborFloor = true;
                                }
                            } else if( heightDiff<0 ) {
                                if( world.getBlock(x,block.height()+3, z).isPenetrable() ) {
                                    foundNeighborFloor = true;
                                }
                            } else if( heightDiff==0 ) {
                                foundNeighborFloor = true;
                            }
                            break;
                        }
                    }
                    if( foundNeighborFloor ) {
                        floor.setContour(block);
                    }
                }
            }
        }
        for (Floor floor : floors) {
            floor.findContour();
        }
    }

    private void connectToNeighbor( WalkableBlock block, int dx, int dz, HeightMap neighbor, int neighborId ) {
        HeightMapCell neighborCell = neighbor.getCell(dx, dz);
        Floor floor = block.floor;

        for (WalkableBlock candidate : neighborCell.blocks) {
            if( Math.abs(candidate.height()-block.height())<2 && candidate.floor.heightMap!=this) {
                block.neighbors[neighborId] = candidate;
                candidate.neighbors[(neighborId+4)%8] = block;

                floor.addNeighborBlock(block, candidate);
                candidate.floor.addNeighborBlock(candidate, block);
            }
        }
    }

    public void disconnectNeighborMaps( HeightMap left, HeightMap up, HeightMap right, HeightMap down ) {
        for (WalkableBlock block : borderBlocks) {
            int x = TeraMath.calcBlockPosX(block.getBlockPosition().x);
            int z = TeraMath.calcBlockPosZ(block.getBlockPosition().z);
            if( left!=null && x==0 ) {
                disconnectFromNeighbor(block, HeightMap.SIZE_X - 1, z, left, DIR_LEFT);
            }
            if( right!=null && x==HeightMap.SIZE_X-1 ) {
                disconnectFromNeighbor(block, 0, z, right, DIR_RIGHT);
            }
            if( up!=null && z==0 ) {
                disconnectFromNeighbor(block, x, HeightMap.SIZE_Z - 1, up, DIR_UP);
            }
            if( down!=null && z==HeightMap.SIZE_Z-1 ) {
                disconnectFromNeighbor(block, x, 0, down, DIR_DOWN);
            }
        }
        if( left!=null ) left.findContour();
        if( right!=null ) right.findContour();
        if( up!=null ) up.findContour();
        if( down!=null ) down.findContour();
    }

    private void disconnectFromNeighbor(WalkableBlock block, int dx, int dz, HeightMap neighbor, int neighborId) {
        HeightMapCell neighborCell = neighbor.getCell(dx, dz);
        Floor floor = block.floor;
        for (WalkableBlock candidate : neighborCell.blocks) {
            if( Math.abs(candidate.height()-block.height())<2 && candidate.floor.heightMap!=this) {
                block.neighbors[neighborId] = null;
                candidate.neighbors[(neighborId+4)%8] = null;
                floor.removeNeighborBlock(block, candidate);
                candidate.floor.removeNeighborBlock(candidate, block);
            }
        }
    }




    public HeightMapCell getCell(int x, int z) {
        return cells[x + z * Chunk.SIZE_Z];
    }

    public WalkableBlock getBlock( int x, int y, int z ) {
        return getCell(x-worldPos.x, z-worldPos.z).getBlock(y-worldPos.y);
    }

    public Floor getFloor( int id ) {
        for (Floor floor : floors) {
            if( floor.id==id ){
                return floor;
            }
        }
        return null;
    }

    public String toString() {
        return walkableBlocks.size()+" walkable blocks"+floors.size()+" floors";
    }

}
