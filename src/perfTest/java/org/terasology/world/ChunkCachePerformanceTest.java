package org.terasology.world;

import org.lwjgl.Sys;
import org.terasology.game.Terasology;
import org.terasology.logic.world.*;

import javax.vecmath.Vector3d;


public class ChunkCachePerformanceTest extends junit.framework.TestCase{
    private final int SIZE = 2000;
    private final Chunk[] chunks = new Chunk[SIZE];
    private long timerTicksPerSecond;
    LocalWorldProvider lwp;

    void init(){
        Terasology.getInstance().init();
        timerTicksPerSecond = Sys.getTimerResolution();
        lwp = new LocalWorldProvider("test", "Blockmaina42");
        for(int k = 0; k < SIZE; ++k){
            chunks[k] = new Chunk(lwp, k, 0, 0);
        }
    }
    void shutdown(){
        Terasology.getInstance().shutdown();
    }

    public void testChunkCash() {
        init();
        benchmarkCache(new ChunkCacheGZip());
        benchmarkCache(new ChunkCacheUncompressed());
        benchmarkCache(new ChunkCacheDeflate());
        benchmarkCache(new ChunkCacheFileSystem(lwp));
        shutdown();
    }

    void benchmarkCache(IChunkCache cc){
        long t1 = Sys.getTime();
        for(int k = 0; k < SIZE; ++k){
            cc.put(chunks[k]);
        }
        long t2 = Sys.getTime();
        for(int k = 0; k < SIZE; ++k){
            assertTrue(cc.get(chunks[k].getId()) != null);
        }
        long t3 = Sys.getTime();

        System.out.printf("%s in memory cache size is %fMb\n", cc.getClass(), cc.size());
        System.out.printf("%s write time per chunk is %f ms\n", cc.getClass(), ((float)(t2-t1)/timerTicksPerSecond)/(float)SIZE*1000);
        System.out.printf("%s read time per chunk is %f ms\n", cc.getClass(), ((float)(t3-t2)/timerTicksPerSecond)/(float)SIZE*1000);
    }
}

