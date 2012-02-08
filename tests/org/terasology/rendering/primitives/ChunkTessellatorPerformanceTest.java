package org.terasology.rendering.primitives;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.terasology.logic.manager.ConfigurationManager;
import org.terasology.logic.world.Chunk;
import org.terasology.logic.world.LocalWorldProvider;
import org.terasology.logic.world.WorldProvider;
import org.terasology.performanceMonitor.PerformanceMonitor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */

public class ChunkTessellatorPerformanceTest {

    @BeforeClass
    public static void beforeClass() throws Exception
    {
        if (System.getProperty("os.name").equals("Mac OS X"))
            addLibraryPath("natives/macosx");
        else if (System.getProperty("os.name").equals("Linux"))
            addLibraryPath("natives/linux");
        else
            addLibraryPath("natives/windows");

        Display.setDisplayMode(new DisplayMode(0,0));
        Display.create();
    }

    @Test
    public void performanceTestChunkTessellator() throws Exception
    {
        WorldProvider worldProv = new LocalWorldProvider("Test", "ohnomelons");
        long totalTime = 0;
        PerformanceMonitor.setEnabled(true);
        for (int x = -1; x < 2; ++x)
        {
            for (int z = -1; z < 7; ++z)
            {
                Chunk chunk = worldProv.getChunkProvider().loadOrCreateChunk(x,z);
                chunk.generate();
                chunk.updateLight();
            }
        }

        for (int i = 0; i < 5; ++i)
        {
            Chunk chunk = worldProv.getChunkProvider().loadOrCreateChunk(0,i);
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

    private static void addLibraryPath(String s) throws Exception {
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        final String[] paths = (String[]) usrPathsField.get(null);

        for (String path : paths) {
            if (path.equals(s)) {
                return;
            }
        }

        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length - 1] = s;
        usrPathsField.set(null, newPaths);
    }
}
