package org.terasology.pathfinding.model;

import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

/**
 * @author synopia
 */
public class WalkableBlockFinder {
    private WorldProvider world;

    public WalkableBlockFinder(WorldProvider world) {
        this.world = world;
    }

    public void findWalkableBlocks(HeightMap map) {
        int []airMap = new int[HeightMap.SIZE_X*HeightMap.SIZE_Z];
        Vector3i blockPos = new Vector3i();
        map.walkableBlocks.clear();
        Vector3i worldPos = map.worldPos;
        for (int y = HeightMap.SIZE_Y-1; y >= 0; y--) {
            for (int z = 0; z < HeightMap.SIZE_Z; z++) {
                for (int x = 0; x < HeightMap.SIZE_X; x++) {
                    blockPos.set(x+worldPos.x, y+worldPos.y, z+worldPos.z);
                    Block block = world.getBlock(blockPos);
                    int offset = x + z * Chunk.SIZE_Z;
                    if( block.isPenetrable() ) {
                        airMap[offset]++;
                    } else {
                        if( airMap[offset]>=2 ) {
                            WalkableBlock walkableBlock = new WalkableBlock(blockPos.x, blockPos.z, blockPos.y);
                            map.cells[offset].addBlock(walkableBlock);
                            map.walkableBlocks.add(walkableBlock);
                        }
                        airMap[offset] = 0;
                    }
                }
            }
        }

        findNeighbors(map);
    }

    private void findNeighbors(HeightMap map) {
        map.borderBlocks.clear();
        for (int z = 0; z < Chunk.SIZE_Z; z++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                int offset = x + z * Chunk.SIZE_Z;
                HeightMapCell cell = map.cells[offset];
                for (WalkableBlock block : cell.blocks) {
                    for (int i = 0; i < HeightMap.DIRECTIONS.length; i++) {
                        connectToDirection(map, x, z, block, i);
                    }
//                    for (int i = 1; i < HeightMap.DIRECTIONS.length; i+=2) {
//                        connectToDirectionDiagonal(map, x, z, block, i);
//                    }
                }
            }
        }
    }

    private void connectToDirection(HeightMap map, int x, int z, WalkableBlock block, int direction) {
        int dx = HeightMap.DIRECTIONS[direction][0];
        int dy = HeightMap.DIRECTIONS[direction][1];
        int nx = x + dx;
        int nz = z + dy;
        if( nx<0 || nz<0 || nx>= HeightMap.SIZE_X || nz>=HeightMap.SIZE_Z ) {
            map.borderBlocks.add(block);
            return;
        }
        HeightMapCell neighbor = map.cells[nx + nz * HeightMap.SIZE_Z];
        for (WalkableBlock neighborBlock : neighbor.blocks) {
            connectBlocks(block, neighborBlock, direction);
        }
    }
    private void connectBlocks( WalkableBlock block, WalkableBlock neighborBlock, int direction ) {
        int heightDiff = block.height() - neighborBlock.height();
        boolean diagonal = (direction%2)==1;
        if( heightDiff==0 ) {
            if( !diagonal ) {
                block.neighbors[direction] = neighborBlock;
            } else {
                int dx = block.x()-neighborBlock.x();
                int dz = block.z()-neighborBlock.z();
                boolean free1 = world.getBlock(block.x()-dx, block.height()+1, block.z()).isPenetrable();
                free1 &= world.getBlock(block.x()-dx, block.height()+2, block.z()).isPenetrable();
                boolean free2 = world.getBlock(block.x(), block.height()+1, block.z()-dz).isPenetrable();
                free2 &= world.getBlock(block.x(), block.height()+2, block.z()-dz).isPenetrable();
                if( free1&&free2 ) {
                    block.neighbors[direction] = neighborBlock;
                }
            }
        } else if( Math.abs(heightDiff)<2 && !diagonal ) {
            WalkableBlock lower = heightDiff < 0 ? block : neighborBlock;
            WalkableBlock higher = heightDiff < 0 ? neighborBlock : block;
            Block jumpCheck = world.getBlock(lower.x(), lower.height()+3, lower.z());
            if( jumpCheck.isPenetrable() ) {
                block.neighbors[direction] = neighborBlock;
            }
        }
    }

}
