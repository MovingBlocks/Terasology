package org.terasology.benchmark.chunks.arrays;

import org.terasology.benchmark.BenchmarkResult;
import org.terasology.world.chunks.blockdata.TeraArray;

/**
 * BenchmarkTeraArrayRead implements a simple read performance benchmark for tera arrays.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public class BenchmarkTeraArrayRead extends BenchmarkTeraArray {

    public BenchmarkTeraArrayRead(TeraArray array) {
        super(array);
    }
    
    @Override
    public String getTitle() {
        return array.getClass().getSimpleName() + " read performance";
    }

    @Override
    public int run(final int index, final int repetitions, final BenchmarkResult result) {
        int tmp = 0;
        for (int i = 0; i < repetitions; i++) {
            for (int y = 0; y < array.getSizeY(); y++) {
                for (int z = 0; z < array.getSizeZ(); z++) {
                    for (int x = 0; x < array.getSizeX(); x++) {
                        tmp += array.get(x, y, z);
                        array.get(x, y, z);
                    }
                }
            }
        }
        return tmp;
    }

}
