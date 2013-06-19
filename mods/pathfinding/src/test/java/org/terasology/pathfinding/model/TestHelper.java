package org.terasology.pathfinding.model;

import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.*;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.ChunkGenerator;
import org.terasology.world.liquid.LiquidData;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
public class TestHelper {
    public HeightMap map;
    public TestWorld world;
    public int sizeX;
    public int sizeY;
    public int sizeZ;

    public TestHelper() {
        this(null);
    }
    public TestHelper(ChunkGenerator generator) {
        CoreRegistry.put(Config.class, new Config());
        world = new TestWorld(generator);
        map = new HeightMap(world, new Vector3i(0,0,0));
    }

    public void setGround(int x, int y, int z) {
        world.setBlock(x,y,z, world.ground, null);
    }
    public void setAir(int x, int y, int z) {
        world.setBlock(x,y,z, world.air, null);
    }

    public void setGround(String... lines) {
        parse(new Runner() {
            @Override
            public char run(int x, int y, int z, char value) {
                switch (value) {
                    case 'X':
                        setGround(x, y, z);
                        break;
                    case ' ':
                        setAir(x,y,z);
                        break;
                }
                return 0;
            }
        }, lines);
    }

    public String[] evaluate(Runner runner) {
        return evaluate(runner, 0,0,0, sizeX, sizeY, sizeZ);
    }
    public String[] evaluate(Runner runner, int xs, int ys, int zs, int sizeX, int sizeY, int sizeZ) {
        String[][] table = new String[sizeY][sizeZ];
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                StringBuilder line = new StringBuilder();
                for (int x = 0; x < sizeX; x++) {
                    char value = runner.run(x+xs, y+ys, z+zs, (char) 0);
                    line.append(value);
                }
                table[y][z] = line.toString();
            }
        }
        return combine("|", table);
    }

    public void run() {
        map.update();
    }

    public void parse(Runner runner, String... lines) {
        String[][] expected  = split("\\|", lines);
        sizeX=0;
        sizeY=0;
        sizeZ=0;

        for( int y = 0; y < expected.length; y++ ) {
            if( y>sizeY ) {
                sizeY=y;
            }
            for (int z = 0; z < expected[y].length; z++) {
                if( z>sizeZ ) {
                    sizeZ=z;
                }
                String line = expected[y][z];
                for (int x = 0; x < line.length(); x++) {
                    if( x>sizeX ) {
                        sizeX=x;
                    }
                    char c = line.charAt(x);
                    runner.run(x,y,z,c);
                }
            }
        }
        sizeX++;
        sizeY++;
        sizeZ++;
    }

    public static String[][] split(String separator, String ...lines) {
        List<List<String>> table = new ArrayList<List<String>>();
        for (String line : lines) {
            if( line==null || line.length()==0 ) {
                continue;
            }
            String[] parts = line.split(separator);
            for (int i = table.size(); i < parts.length; i++) {
                table.add(new ArrayList<String>());
            }
            for (int i = 0; i < parts.length; i++) {
                table.get(i).add(parts[i]);
            }
        }
        String[][] result = new String[table.size()][lines.length];
        for (int i = 0; i < table.size(); i++) {
            List<String> col = table.get(i);
            for (int j = 0; j < col.size(); j++) {
                result[i][j] = col.get(j);
            }
        }
        return result;
    }

    public static String[] combine( String separator, String[][] table ) {
        String[] result = new String[table[0].length];
        for (int z = 0; z < table[0].length; z++) {
            StringBuilder line = new StringBuilder();
            for (int y = 0; y < table.length; y++) {
                if( y!=0 ) {
                    line.append(separator);
                }
                line.append(table[y][z]);
            }
            result[z] = line.toString();
        }
        return result;
    }

    public static class TestWorld implements WorldProvider {
        private Map<Vector3i, Block> blocks = new HashMap<Vector3i, Block>();
        private Map<Vector3i, Chunk> chunks = new HashMap<Vector3i, Chunk>();
        private ChunkGenerator chunkGenerator;
        public Block air;
        public Block ground;

        public TestWorld() {
            air = new Block();
            air.setPenetrable(true);

            ground = new Block();
            ground.setPenetrable(false);

            BlockManager.getInstance().addBlockFamily(new SymmetricFamily(new BlockUri("air"), air) );
            BlockManager.getInstance().addBlockFamily(new SymmetricFamily(new BlockUri("ground"), ground) );

        }
        public TestWorld(ChunkGenerator chunkGenerator) {
            this();
            this.chunkGenerator = chunkGenerator;
        }

        @Override
        public boolean isBlockActive(Vector3i pos) {
            return true;
        }

        @Override
        public boolean isBlockActive(Vector3f pos) {
            return true;
        }

        @Override
        public boolean setBlock(Vector3i pos, Block type, Block oldType) {
            blocks.put(pos, type);
            return true;
        }

        @Override
        public boolean setLiquid(Vector3i pos, LiquidData state, LiquidData oldState) {
            return false;
        }

        @Override
        public LiquidData getLiquid(Vector3i blockPos) {
            return null;
        }

        @Override
        public Block getBlock(Vector3f pos) {
            return getBlock(new Vector3i((int) pos.x, (int) pos.y, (int) pos.z));
        }

        @Override
        public Block getBlock(Vector3i pos) {
            Block block = blocks.get(pos);
            if( block!=null ) {
                return block;
            }
            Vector3i chunkPos = TeraMath.calcChunkPos(pos);
            Chunk chunk = chunks.get(chunkPos);
            if( chunk==null && chunkGenerator !=null ) {
                chunk = new Chunk(chunkPos);
                chunkGenerator.generateChunk(chunk);
                chunks.put(chunkPos, chunk);
            }
            if( chunk!=null ) {
                return chunk.getBlock(TeraMath.calcBlockPos(pos.x, pos.y, pos.z));
            }
            return air;
        }

        @Override
        public byte getLight(Vector3f pos) {
            return 0;
        }

        @Override
        public byte getSunlight(Vector3f pos) {
            return 0;
        }

        @Override
        public byte getTotalLight(Vector3f pos) {
            return 0;
        }

        @Override
        public byte getLight(Vector3i pos) {
            return 0;
        }

        @Override
        public byte getSunlight(Vector3i pos) {
            return 0;
        }

        @Override
        public byte getTotalLight(Vector3i pos) {
            return 0;
        }

        @Override
        public String getTitle() {
            return "";
        }

        @Override
        public String getSeed() {
            return "1";
        }

        @Override
        public WorldInfo getWorldInfo() {
            return null;
        }

        @Override
        public WorldBiomeProvider getBiomeProvider() {
            return null;
        }

        @Override
        public WorldView getLocalView(Vector3i chunk) {
            return null;
        }

        @Override
        public WorldView getWorldViewAround(Vector3i chunk) {
            return null;
        }

        @Override
        public boolean isBlockActive(int x, int y, int z) {
            return true;
        }

        @Override
        public boolean setBlocks(BlockUpdate... updates) {
            for (BlockUpdate update : updates) {
                setBlock(update.getPosition(), update.getNewType(), update.getOldType());
            }
            return true;
        }

        @Override
        public boolean setBlocks(Iterable<BlockUpdate> updates) {
            for (BlockUpdate update : updates) {
                setBlock(update.getPosition(), update.getNewType(), update.getOldType());
            }
            return true;
        }

        @Override
        public boolean setBlock(int x, int y, int z, Block type, Block oldType) {
            setBlock(new Vector3i(x, y, z), type, oldType);
            return true;
        }

        @Override
        public boolean setLiquid(int x, int y, int z, LiquidData newData, LiquidData oldData) {
            return false;
        }

        @Override
        public LiquidData getLiquid(int x, int y, int z) {
            return null;
        }

        @Override
        public Block getBlock(int x, int y, int z) {
            return getBlock(new Vector3i(x,y,z));
        }

        @Override
        public byte getLight(int x, int y, int z) {
            return 0;
        }

        @Override
        public byte getSunlight(int x, int y, int z) {
            return 0;
        }

        @Override
        public byte getTotalLight(int x, int y, int z) {
            return 0;
        }

        @Override
        public long getTime() {
            return 0;
        }

        @Override
        public void setTime(long time) {
        }

        @Override
        public float getTimeInDays() {
            return 0;
        }

        @Override
        public void setTimeInDays(float time) {
        }

        @Override
        public void dispose() {
        }
    }

    public interface Runner {
        public char run( int x, int y, int z, char value );
    }



}
