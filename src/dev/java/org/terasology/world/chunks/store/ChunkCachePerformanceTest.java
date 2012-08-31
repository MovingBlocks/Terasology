package org.terasology.world.chunks.store;

import org.terasology.TerasologyDevelopment;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkStore;

public final class ChunkCachePerformanceTest {

    private static final String SUB_FOLDER = ChunkCachePerformanceTest.class.getSimpleName();
    private static final int SIZE = 1000;

    private final Chunk[] chunks;

    public ChunkCachePerformanceTest(final int size) {
        chunks = new Chunk[size];
        for (int k = 0; k < size; ++k) {
            chunks[k] = new Chunk(k, 0, 0);
        }
    }

    public void testChunkCash() {
        benchmarkCache(new ChunkStoreGZip());
        benchmarkCache(new ChunkStoreUncompressed());
        benchmarkCache(new ChunkStoreDeflate());
        benchmarkCache(new ChunkStoreFileSystem(TerasologyDevelopment.getOutputFolder(SUB_FOLDER)));
    }

    private void benchmarkCache(final ChunkStore chunkStore) {
        final int size = chunks.length;
        System.out.println("Start test: " + chunkStore + " with size=" + size);
        try {
            final long t1 = System.nanoTime();
            for (int k = 0; k < size; ++k) {
                chunkStore.put(chunks[k]);
            }
            final long t2 = System.nanoTime();
            for (int k = 0; k < size; ++k) {
                if (chunkStore.get(chunks[k].getPos()) == null) {
                    throw new RuntimeException("Cannot load chunk! k=" + k + ", chunkStore=" + chunkStore);
                }
            }
            final long t3 = System.nanoTime();

            System.out.printf("%s in memory cache size is %fMb\n", chunkStore.getClass(), chunkStore.size());
            System.out.printf("%s write time per chunk is %f ms\n", chunkStore.getClass(), (((t2 - t1) / 10e9) / size) * 1000);
            System.out.printf("%s read time per chunk is %f ms\n", chunkStore.getClass(), (((t3 - t2) / 10e9) / size) * 1000);
        } finally {
            final long t4 = System.nanoTime();
            chunkStore.dispose();
            final long t5 = System.nanoTime();
            System.out.printf("%s dispose time per chunk is %f ms\n", chunkStore.getClass(), (((t5 - t4) / 10e9) / size) * 1000);
        }
    }

    public static void main(final String[] args) {
        try {
            final ChunkCachePerformanceTest chunkCachePerformanceTest = new ChunkCachePerformanceTest(SIZE);
            chunkCachePerformanceTest.testChunkCash();
        } catch (final RuntimeException e) {
            System.err.println("ChunkCachePerformanceTest failed! " + e.getMessage());
        }
    }

}
