package org.terasology.rendering.gui.windows.metricsScreen;

import org.terasology.rendering.gui.widgets.UILabel;

import java.util.List;

/**
 * @author Immortius
 */
final class NullMode extends MetricsMode {

    public NullMode() {
        super("", false, false);
    }

    @Override
    public void updateLines(List<UILabel> lines) {
        for (UILabel line : lines) {
            line.setVisible(false);
        }
    }
}
