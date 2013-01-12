package org.terasology.rendering.gui.windows.metricsScreen;

import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.gui.widgets.UILabel;

import java.util.List;

/**
 * @author Immortius
 */
final class RunningMeansMode extends MetricsMode {

    public RunningMeansMode() {
        super("Running means", true, true);
    }

    @Override
    public void updateLines(List<UILabel> lines) {
        displayMetrics(PerformanceMonitor.getRunningMean(), lines);
    }
}
