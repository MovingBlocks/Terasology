package org.terasology.rendering.gui.windows.metricsScreen;

import gnu.trove.procedure.TObjectIntProcedure;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.gui.widgets.UILabel;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Immortius
 */
final class RunningThreadsMode extends MetricsMode {

    public RunningThreadsMode() {
        super("Running Threads", true, true);
    }

    @Override
    public void updateLines(List<UILabel> lines) {
        final SortedSet<String> threads = new TreeSet<String>();
        PerformanceMonitor.getRunningThreads().forEachEntry(new TObjectIntProcedure<String>() {
            public boolean execute(String s, int i) {
                threads.add(String.format("%s (%d)", s, i));
                return true;
            }
        });
        int line = 0;
        for (String thread : threads) {
            lines.get(line).setVisible(true);
            lines.get(line).setText(thread);
            line++;
            if (line >= lines.size()) break;
        }
        for (; line < lines.size(); line++) {
            lines.get(line).setVisible(false);
        }
    }
}
