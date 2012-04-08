package org.terasology.rendering.primitives;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.procedure.TObjectDoubleProcedure;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.Sys;
import org.terasology.game.Terasology;
import org.terasology.logic.world.Chunk;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.logic.world.LocalWorldProvider;
import org.terasology.performanceMonitor.PerformanceMonitor;

/**
 * @author Immortius <immortius@gmail.com>
 */

public class ChunkTessellatorPerformanceTest {
	public static void main(String[] args) throws Exception {
		ChunkTessellatorPerformanceTest test = new ChunkTessellatorPerformanceTest();
		
		test.performanceTestChunkTessellator();
	}
    public void performanceTestChunkTessellator() throws Exception
    {
    	Terasology.getInstance().init();
        IWorldProvider worldProv = new LocalWorldProvider("Test", "ohnomelons");
        long totalTime = 0;
        PerformanceMonitor.setEnabled(false);
        for (int x = -1; x < 2; ++x)
        {
            for (int z = -1; z < 7; ++z)
            {
                Chunk chunk = worldProv.getChunkProvider().getChunk(x, z);
                chunk.generate();
                chunk.updateLight();
            }
        }

        for (int i = 0; i < 5; ++i)
        {
            Chunk chunk = worldProv.getChunkProvider().getChunk(0, i);
            long startTime = Sys.getTime();
            chunk.generateMeshes();
            long endTime = Sys.getTime();
            totalTime += endTime - startTime;
        }
        double time = 200.0 * totalTime / Sys.getTimerResolution();
        System.out.println(String.format("TestTime: %.2fms", time));
        List<String> activities = new ArrayList<String>();
        List<Double> values = new ArrayList<Double>();
        PerformanceMonitor.rollCycle();
        sortMetrics(PerformanceMonitor.getRunningMean(), activities, values);
        for (int i = 0; i < activities.size(); ++i)
        {
            System.out.println(String.format("%s: %.2fms", activities.get(i), values.get(i)));
        }
        Terasology.getInstance().shutdown();
    }

    private static void sortMetrics(TObjectDoubleMap<String> metrics, final List<String> activities, final List<Double> values) {
        metrics.forEachEntry(new TObjectDoubleProcedure<String>() {
            public boolean execute(String s, double v) {
                boolean inserted = false;
                for (int i = 0; i < values.size(); i++)
                {
                    if (v > values.get(i))
                    {
                        values.add(i, v);
                        activities.add(i, s);
                        inserted = true;
                        break;
                    }
                }
                if (!inserted)
                {
                    activities.add(s);
                    values.add(v);
                }
                return true;
            }
        });
    }
}
