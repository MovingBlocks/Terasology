package org.terasology.pathfinding.model;

import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;

import java.util.ArrayList;
import java.util.List;

/**
 * @author synopia
 */
public class WalkableBlockFinder {
    private WorldProvider world;

    public WalkableBlockFinder(WorldProvider world) {
        this.world = world;
    }

    public void findWalkableBlocks(HeightMap map) {
        int airMap[] = new int[HeightMap.SIZE_X*HeightMap.SIZE_Z];
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
                findNeighbor(map, cell, x, z);
            }
        }
    }

    private void findNeighbor(HeightMap map, HeightMapCell cell, int x, int z) {
        for (WalkableBlock block : cell.blocks) {
            for (int i = 0; i < HeightMap.DIRECTIONS.length; i++) {
                int nx = x + HeightMap.DIRECTIONS[i][0];
                int nz = z + HeightMap.DIRECTIONS[i][1];
                if( nx<0 || nz<0 || nx>= HeightMap.SIZE_X || nz>=HeightMap.SIZE_Z ) {
                    map.borderBlocks.add(block);
                    continue;
                }
                HeightMapCell neighbor = map.cells[nx + nz * HeightMap.SIZE_Z];
                for (WalkableBlock neighborBlock : neighbor.blocks) {
                    int heightDiff = block.height() - neighborBlock.height();
                    if( heightDiff>-2 && heightDiff<2 ) {
                        if( heightDiff>0 ) {
                            Block air = world.getBlock(nx, neighborBlock.height() + 3, nz);
                            if( air.isPenetrable() ) {
                                block.neighbors[i] = neighborBlock;
                            }
                        } else if( heightDiff<0 ) {
                            Block air = world.getBlock(x, block.height() + 3, z);
                            if( air.isPenetrable() ) {
                                block.neighbors[i] = neighborBlock;
                            }
                        } else {
                            block.neighbors[i] = neighborBlock;
                        }
                    }
                }
            }
        }
    }

}
