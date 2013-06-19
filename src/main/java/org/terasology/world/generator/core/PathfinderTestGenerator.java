package org.terasology.world.generator.core;

import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.ChunkGenerator;

import java.util.Map;

/**
 * @author synopia
 */
public class PathfinderTestGenerator implements ChunkGenerator {
    public Block air = BlockManager.getInstance().getAir();
    public Block ground = BlockManager.getInstance().getBlock("engine:Dirt");
    public boolean generateStairs;

    public PathfinderTestGenerator() {
        this(true);
    }

    public PathfinderTestGenerator(boolean generateStairs) {
        this.generateStairs = generateStairs;
    }

    @Override
    public void generateChunk(Chunk chunk) {
        generateLevel(chunk, 50);

        generateLevel(chunk, 45);
        if( generateStairs ) {
            generateStairs(chunk, 45);
        }
    }

    private void generateLevel(Chunk chunk, int groundHeight) {
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                chunk.setBlock(x,groundHeight,z,ground);

                for (int y = groundHeight+1; y < groundHeight+4; y++) {
                    chunk.setBlock(x,y,z,air);
                }
            }
        }
        for (int x = 0; x < Chunk.SIZE_X; x++) {
            chunk.setBlock(x, groundHeight+1, 0, ground);
            chunk.setBlock(x, groundHeight+1, Chunk.SIZE_Z-1, ground);
        }
        for (int z = 0; z < Chunk.SIZE_Z; z++) {
            chunk.setBlock(0, groundHeight+1, z, ground);
            chunk.setBlock(Chunk.SIZE_X-1, groundHeight+1, z, ground);
        }
    }

    private void generateStairs(Chunk chunk, int groundHeight ) {
        for (int height = groundHeight+1; height < groundHeight+4; height++) {
            for (int x = 0; x < Chunk.SIZE_X; x++) {
                chunk.setBlock(x, height, 0, ground);
                chunk.setBlock(x, height, Chunk.SIZE_Z-1, ground);
            }
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                chunk.setBlock(0, height, z, ground);
                chunk.setBlock(Chunk.SIZE_X-1, height, z, ground);
            }
        }
        int height = groundHeight+1;
        chunk.setBlock(7, height, 0, air);
        chunk.setBlock(8, height, 0, air);
        chunk.setBlock(7, height, Chunk.SIZE_Z-1, air);
        chunk.setBlock(8, height, Chunk.SIZE_Z-1, air);
        chunk.setBlock(0, height, 7, air);
        chunk.setBlock(0, height, 8, air);
        chunk.setBlock(Chunk.SIZE_X-1, height, 7, air);
        chunk.setBlock(Chunk.SIZE_X-1, height, 8, air);

        buildWalkable(chunk, 6, height,   7);
        buildWalkable(chunk, 6, height,   8);
        buildWalkable(chunk, 7, height + 1, 7);
        buildWalkable(chunk, 7, height + 1, 8);
        buildWalkable(chunk, 8, height + 2, 7);
        buildWalkable(chunk, 8, height + 2, 8);
        buildWalkable(chunk, 9, height + 3, 7);
        buildWalkable(chunk, 9, height + 3, 8);
    }

    private void buildWalkable( Chunk chunk, int x, int y, int z ) {
        chunk.setBlock(x, y, z, ground);
        chunk.setBlock(x, y+1, z, air);
        chunk.setBlock(x, y+2, z, air);
    }

    @Override
    public void setWorldSeed(String seed) {

    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider biomeProvider) {

    }

    @Override
    public Map<String, String> getInitParameters() {
        return null;
    }

    @Override
    public void setInitParameters(Map<String, String> initParameters) {
    }
}
