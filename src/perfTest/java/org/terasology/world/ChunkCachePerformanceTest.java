package org.terasology.world;

import java.io.File;

import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkStore;
import org.terasology.world.chunks.store.ChunkStoreDeflate;
import org.terasology.world.chunks.store.ChunkStoreFileSystem;
import org.terasology.world.chunks.store.ChunkStoreGZip;
import org.terasology.world.chunks.store.ChunkStoreUncompressed;


public class ChunkCachePerformanceTest extends junit.framework.TestCase{
    private final int SIZE = 200;
    private final Chunk[] chunks = new Chunk[SIZE];


    void init(){
        for(int k = 0; k < SIZE; ++k){
            chunks[k] = new Chunk(k, 0, 0);
        }
    }

    void shutdown(){
    }

    public void testChunkCash() {
        init();
        benchmarkCache(new ChunkStoreGZip());
        benchmarkCache(new ChunkStoreUncompressed());
        benchmarkCache(new ChunkStoreDeflate());
        benchmarkCache(new ChunkStoreFileSystem(new File("benchmark-temp")));
        shutdown();
    }

    void benchmarkCache(ChunkStore cc){
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

