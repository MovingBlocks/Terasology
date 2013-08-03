/*
 * Copyright 2013 Moving Blocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.gui.windows.metricsScreen;

import com.google.common.collect.Lists;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;
import java.util.ArrayList;
import java.util.List;

/**
 * UI element that graphs performance metrics
 *
 * @author Immortius <immortius@gmail.com>
 *         <p/>
 *         TODO create a debug window
 */
public class UIScreenMetrics extends UIWindow {

    public static final int METRIC_LINES = 10;

    private List<MetricsMode> modes = Lists.newArrayList(new NullMode(), new RunningMeansMode(), new SpikesMode(), new RunningThreadsMode(), new NetworkStatsMode());
    private int currentMode = 0;

    /* DISPLAY ELEMENTS */
    private final UILabel headerLine;
    private final List<UILabel> metricLines;

    /**
     * Init. the HUD.
     */
    public UIScreenMetrics() {
        setId("metrics");
        setSize(new Vector2f(100, 300));
        headerLine = new UILabel();
        headerLine.setPosition(new Vector2f(4, 70));
        addDisplayElement(headerLine);
        metricLines = new ArrayList<UILabel>();
        for (int i = 0; i < METRIC_LINES; ++i) {
            UILabel line = new UILabel();
            line.setPosition(new Vector2f(4, 86 + 16 * i));
            metricLines.add(line);
            addDisplayElement(line);
        }

        update();
        setModal(false);
    }


    /**
     * Renders the HUD on the screen.
     */
    @Override
    public void render() {
        super.render();
    }

    @Override
    public void update() {
        super.update();

        MetricsMode mode = modes.get(currentMode);

        headerLine.setVisible(mode.isVisible());
        headerLine.setText(mode.getDisplayText());
        mode.updateLines(metricLines);
    }

    public void toggleMode() {
        currentMode = (currentMode + 1) % modes.size();
        while (!modes.get(currentMode).isAvailable()) {
            currentMode = (currentMode + 1) % modes.size();
        }
        PerformanceMonitor.setEnabled(modes.get(currentMode).isPerformanceManagerMode());
    }

}
