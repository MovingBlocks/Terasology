package org.terasology.world;

import org.lwjgl.Sys;
import org.terasology.game.TerasologyEngine;
import org.terasology.logic.newWorld.NewChunk;
import org.terasology.logic.newWorld.NewChunkCache;
import org.terasology.logic.newWorld.chunkCache.ChunkCacheDeflate;
import org.terasology.logic.newWorld.chunkCache.ChunkCacheFileSystem;
import org.terasology.logic.newWorld.chunkCache.ChunkCacheGZip;
import org.terasology.logic.newWorld.chunkCache.ChunkCacheUncompressed;
import org.terasology.logic.world.*;

import javax.vecmath.Vector3d;
import java.io.File;


public class ChunkCachePerformanceTest extends junit.framework.TestCase{
    private final int SIZE = 2000;
    private final NewChunk[] chunks = new NewChunk[SIZE];


    void init(){
        for(int k = 0; k < SIZE; ++k){
            chunks[k] = new NewChunk(k, 0, 0);
        }
    }

    void shutdown(){
    }

    public void testChunkCash() {
        init();
        benchmarkCache(new ChunkCacheGZip());
        benchmarkCache(new ChunkCacheUncompressed());
        benchmarkCache(new ChunkCacheDeflate());
        benchmarkCache(new ChunkCacheFileSystem(new File("benchmark-temp")));
        shutdown();
    }

    void benchmarkCache(NewChunkCache cc){
        long t1 = System.nanoTime();
        for(int k = 0; k < SIZE; ++k){
            cc.put(chunks[k]);
        }
        long t2 = System.nanoTime();
        for(int k = 0; k < SIZE; ++k){
            assertTrue(cc.get(chunks[k].getPos()) != null);
        }
        long t3 = System.nanoTime();

        System.out.printf("%s in memory cache size is %fMb\n", cc.getClass(), cc.size());
        System.out.printf("%s write time per chunk is %f ms\n", cc.getClass(), ((float)(t2-t1)/10e9)/(float)SIZE*1000);
        System.out.printf("%s read time per chunk is %f ms\n", cc.getClass(), ((float)(t3-t2)/10e9)/(float)SIZE*1000);
    }
}

