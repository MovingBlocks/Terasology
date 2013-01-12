package org.terasology.rendering.gui.windows.metricsScreen;

import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.rendering.gui.widgets.UILabel;

import java.util.List;

/**
 * @author Immortius
 */
final class SpikesMode extends MetricsMode {

    public SpikesMode() {
        super("Spikes", true, true);
    }

    @Override
    public void updateLines(List<UILabel> lines) {
        displayMetrics(PerformanceMonitor.getDecayingSpikes(), lines);
    }
}
