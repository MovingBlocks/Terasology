package org.terasology.world.generator.core;



import java.util.Map;

import javassist.bytecode.stackmap.TypeData;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.utilities.HeightmapFileReader;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.ChunkGenerator;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.liquid.LiquidType;
/**
 * Generates a terrain based on a provided heightmap
 *
 * @author Nym Traveel
 */
public class BasicHMTerrainGenerator implements ChunkGenerator {


    private WorldBiomeProvider biomeProvider;
    private Block air = BlockManager.getInstance().getAir();
    private Block mantle = BlockManager.getInstance().getBlock(
            "engine:MantleStone");
    private Block water = BlockManager.getInstance().getBlock("engine:Water");
    private Block stone = BlockManager.getInstance().getBlock("engine:Stone");
    private Block sand = BlockManager.getInstance().getBlock("engine:Sand");
    private Block grass = BlockManager.getInstance().getBlock("engine:Grass");
    private Block snow = BlockManager.getInstance().getBlock("engine:Snow");
    private ClimateSimulator climate;
    float[][] heightmap;

    @Override
    public void setWorldSeed(String seed) {
        System.out.println("Initialising World"); //Why is this methode called twice?
        try{  heightmap = HeightmapFileReader.readFile("Heightmap.txt", "\n");}
        catch (Exception e){
            e.printStackTrace();
        }
        heightmap = shiftArray(rotateArray(heightmap),-50,-100);
        //try also other combinations with shift and rotate
        //heightmap = rotateArray(heightmap);

        //initialize Climate/humiditymap
        //climate = new ClimateSimulator(heightmap);

    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider biomeProvider){
        this.biomeProvider = biomeProvider;
    }

    @Override
    public Map<String, String> getInitParameters(){
        return null;
    }

    @Override
    public void setInitParameters(Map<String, String> initParameters){
    }



    /**
     * Generate the local contents of a chunk. This should be purely deterministic from the chunk contents, chunk
     * position and world seed - should not depend on external state or other data.
     *
     * @param c
     */
    public void generateChunk(Chunk c){

        int hm_x = (((c.getChunkWorldPosX()/Chunk.SIZE_X)%512)+512)%512;
        int hm_z = (((c.getChunkWorldPosZ()/Chunk.SIZE_Z)%512)+512)%512;

        double scaleFactor = 0.05*Chunk.SIZE_Y;

        double p00 = heightmap[hm_x][hm_z]*scaleFactor;
        double p10 = heightmap[(hm_x-1+512)%512][(hm_z)%512]*scaleFactor;
        double p11 = heightmap[(hm_x-1+512)%512][(hm_z+1+512)%512]*scaleFactor;
        double p01 = heightmap[(hm_x)%512][(hm_z+1+512)%512]*scaleFactor;

        for (int x = 0; x < Chunk.SIZE_X; x++) {
            for (int z = 0; z < Chunk.SIZE_Z; z++) {
                WorldBiomeProvider.Biome type = biomeProvider.getBiomeAt(
                        c.getBlockWorldPosX(x), c.getBlockWorldPosZ(z));

                //calculate avg height
                double interpolatedHeight = lerp(x/(double)Chunk.SIZE_X,lerp(z/(double)Chunk.SIZE_Z,p10,p11), lerp(z/(double)Chunk.SIZE_Z,p00,p01));


                //Scale the height to fit one chunk (suppose we have max height 20 on the Heigthmap
                //ToDo: Change this formula in later implementation of vertical chunks
                double threshold = Math.floor(interpolatedHeight);

                for (int y = Chunk.SIZE_Y; y >= 0; y--) {
                    if (y == 0) { // The very deepest layer of the world is an
                        // indestructible mantle
                        c.setBlock(x, y, z, mantle);
                        break;
                    } else if (y<threshold){
                        c.setBlock(x,y,z,stone);
                    } else if (y==threshold){
                        if (y<Chunk.SIZE_Y*0.05+1){
                            c.setBlock(x,y,z,sand);
                        } else if (y<Chunk.SIZE_Y*0.05*12){
                            c.setBlock(x,y,z,grass);
                        } else{
                            c.setBlock(x,y,z,snow);
                        }
                    } else{
                        if (y <= Chunk.SIZE_Y/20 ) { // Ocean
                            c.setBlock(x, y, z, water);
                            c.setLiquid(x, y, z, new LiquidData(LiquidType.WATER,
                                    Chunk.MAX_LIQUID_DEPTH));

                        } else {
                            c.setBlock(x,y,z,air);
                        }
                    }
                }
            }
        }
    }

    //helper functions for the Mapdesign until real mapGen is in
    public static float[][] rotateArray(float[][] array) {
        float[][] newArray = new float[array[0].length][array.length];
        for (int i=0; i<newArray.length; i++) {
            for (int j=0; j<newArray[0].length; j++) {
                newArray[i][j] = array[j][array[j].length-i-1];
            }
        }
        return newArray;
    }

    public static float[][] shiftArray(float[][] array,int x,int y){
        int size = array.length;
        float[][] newArray = new float[size][size];
        for (int i=0; i<size; i++) {
            for (int j=0; j<size; j++) {
                newArray[i][j] = array[(i+x+size)%size][(j+y+size)%size];
            }
        }
        return newArray;
    }

    private static double lerp(double t, double a, double b) {
        return a + fade(t) * (b - a);  //not sure if i should fade t, needs a bit longer to generate chunks but is definately nicer
    }
    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
}
