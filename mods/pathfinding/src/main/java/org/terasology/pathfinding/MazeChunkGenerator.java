package org.terasology.pathfinding;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.game.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.maze.MazeGenerator;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.WorldView;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.ChunkGenerator;
import org.terasology.world.generator.SecondPassChunkGenerator;

import java.util.*;

/**
 * @author synopia
 */
public class MazeChunkGenerator implements ChunkGenerator, SecondPassChunkGenerator{
    private static final Logger logger = LoggerFactory.getLogger(MazeChunkGenerator.class);

    public Block air = BlockManager.getInstance().getAir();
    public Block ground = BlockManager.getInstance().getBlock("engine:Dirt");
    public Block torch = BlockManager.getInstance().getBlock("engine:Torch.top");

    private int width;
    private int height;
    private List<BitSet[]> mazes;
    private List<Vector3i> stairs;
    private int lastStairX=0;
    private int lastStairY=0;
    private int startHeight;
    private Random random;

    public MazeChunkGenerator(int width, int height, int levels, int startHeight, long seed) {
        this.width = width;
        this.height = height;
        this.startHeight = startHeight;
        this.random = new Random(seed);

        mazes = Lists.newArrayList();
        stairs = Lists.newArrayList();
        for (int i = 0; i < levels; i++) {
            mazes.add(createMaze());
        }
        for (int i = 0; i < mazes.size()-1; i++) {
            insertStairs( i, mazes.get(i), mazes.get(i+1));
        }
    }

    private void insertStairs( int level, BitSet[] level1, BitSet[] level2 ) {
        int free=0;
        int stairX=-1;
        int stairY=-1;
        for (int y = lastStairY; y < height; y++) {
            for (int x = lastStairX; x < width; x++) {
                if( level1[y].get(x) || level2[y].get(x) ) {
                    free = 0;
                } else {
                    free ++;
                }
                if( free==5 ) {
                    stairX = x;
                    stairY = y;
                    break;
                }
            }
            if( stairX!=-1 && stairY!=-1) {
                break;
            }
            lastStairX = 0;
        }
        if( stairX!=-1 && stairY!=-1) {
            stairs.add(new Vector3i(stairX-4, level, stairY));
            lastStairX = stairX;
            lastStairY = stairY;
        }
    }

    private BitSet[] createMaze() {
        BitSet[] maze = new BitSet[height];
        for (int i = 0; i < maze.length; i++) {
            maze[i] = new BitSet(width);
        }
        MazeGenerator generator = new MazeGenerator(width, height, random);
        generator.display(maze);
        return maze;
    }

    @Override
    public void postProcessChunk(Vector3i chunkPos, WorldView view) {
//        placeBlockInBounds(view, chunkPos, 1, 50, 1, start);
//        placeBlockInBounds(view, chunkPos, width-2, 50, height-2, target);
    }

    private void placeBlockInBounds( WorldView view, Vector3i chunkPos, int x, int y, int z, Block block ) {
        int cpx = x-(chunkPos.x<<Chunk.POWER_X);
        int cpz = z-(chunkPos.z<<Chunk.POWER_Z);
        if( cpx>=0 && cpx<Chunk.SIZE_X && cpz>=0 && cpz<Chunk.SIZE_Z ) {
            Vector3i blockPosition = new Vector3i(cpx, y, cpz);
            view.setBlock(blockPosition, air, view.getBlock(blockPosition));
//            CoreRegistry.get(BlockEntityRegistry.class).getOrCreateBlockEntityAt(blockPosition);
        }
    }

    @Override
    public void generateChunk(Chunk chunk) {
        int groundHeight = startHeight;
        int offsetX = chunk.getChunkWorldPosX();
        int offsetZ = chunk.getChunkWorldPosZ();
        int y = groundHeight;
        if( offsetX>=0 && offsetZ>= 0 && offsetX<width && offsetZ<height ) {
            logger.info("generate maze chunk");
            for (BitSet[] maze : mazes) {
                for (int z = 0; z < Chunk.SIZE_Z; z++) {
                    for (int x = 0; x < Chunk.SIZE_X; x++) {
                        int mazeX = offsetX + x;
                        int mazeZ = offsetZ + z;
                        chunk.setBlock(x, y, z, ground);

                        if( mazeX<width && mazeZ<height && maze[mazeZ].get(mazeX) ) {
                            chunk.setBlock(x, y+1, z, ground);
                            chunk.setBlock(x, y+2, z, ground);
                        } else {
                            if( (mazeX%3)==1 && (mazeZ%3)==1 ) {
                                chunk.setBlock(x, y+1, z, torch);
                            } else {
                                chunk.setBlock(x, y+1, z, air);
                            }
                            chunk.setBlock(x, y+2, z, air);
                        }

                    }
                }
                y += 3;
            }
            for (int i = 0; i < stairs.size(); i++) {
                Vector3i stairPos = stairs.get(i);
                int chunkPosX = stairPos.x-offsetX;
                int chunkPosY = groundHeight + stairPos.y*3;
                int chunkPosZ = stairPos.z-offsetZ;
                if( chunkPosX>=0 && chunkPosZ>=0 && chunkPosX<Chunk.SIZE_X && chunkPosZ<Chunk.SIZE_Z ) {
                    chunk.setBlock(chunkPosX, chunkPosY+1, chunkPosZ, air);
                    chunk.setBlock(chunkPosX, chunkPosY+2, chunkPosZ, air);
                    chunk.setBlock(chunkPosX, chunkPosY+3, chunkPosZ, air);
                }
                chunkPosX++;
                if( chunkPosX>=0 && chunkPosZ>=0 && chunkPosX<Chunk.SIZE_X && chunkPosZ<Chunk.SIZE_Z ) {
                    chunk.setBlock(chunkPosX, chunkPosY+1, chunkPosZ, ground);
                    chunk.setBlock(chunkPosX, chunkPosY+2, chunkPosZ, air);
                    chunk.setBlock(chunkPosX, chunkPosY+3, chunkPosZ, air);
                }
                chunkPosX++;
                if( chunkPosX>=0 && chunkPosZ>=0 && chunkPosX<Chunk.SIZE_X && chunkPosZ<Chunk.SIZE_Z ) {
                    chunk.setBlock(chunkPosX, chunkPosY+1, chunkPosZ, air);
                    chunk.setBlock(chunkPosX, chunkPosY+2, chunkPosZ, ground);
                    chunk.setBlock(chunkPosX, chunkPosY+3, chunkPosZ, air);
                }
                chunkPosX++;
                if( chunkPosX>=0 && chunkPosZ>=0 && chunkPosX<Chunk.SIZE_X && chunkPosZ<Chunk.SIZE_Z ) {
                    chunk.setBlock(chunkPosX, chunkPosY+1, chunkPosZ, air);
                    chunk.setBlock(chunkPosX, chunkPosY+2, chunkPosZ, air);
                    chunk.setBlock(chunkPosX, chunkPosY+3, chunkPosZ, ground);
                }
            }
        }
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
