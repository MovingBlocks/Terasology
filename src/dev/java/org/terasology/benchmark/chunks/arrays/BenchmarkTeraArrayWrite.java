package org.terasology.benchmark.chunks.arrays;

import org.terasology.benchmark.BenchmarkResult;
import org.terasology.world.chunks.blockdata.TeraArray;

/**
 * BenchmarkTeraArrayWrite implements a simple write performance benchmark for tera arrays.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public class BenchmarkTeraArrayWrite extends BenchmarkTeraArray {

    public BenchmarkTeraArrayWrite(TeraArray array) {
        super(array);
    }

    @Override
    public String getTitle() {
        return array.getClass().getSimpleName() + " write performance";
    }

    @Override
    public int run(final int index, final int repetitions, final BenchmarkResult result) {
        for (int i = 0; i < repetitions; i++) {
            for (int y = 0; y < array.getSizeY(); y++) {
                for (int z = 0; z < array.getSizeZ(); z++) {
                    for (int x = 0; x < array.getSizeX(); x++) {
                        array.set(x, y, z, index);
                    }
                }
            }
        }
        return 0;
    }

}
