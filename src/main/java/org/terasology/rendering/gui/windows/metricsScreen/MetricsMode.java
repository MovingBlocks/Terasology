/*
 * Copyright 2013 MovingBlocks
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

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import org.terasology.rendering.gui.widgets.UILabel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Immortius
 */
abstract class MetricsMode {
    private String displayText;
    private boolean visible;
    private boolean performanceManagerMode;

    public MetricsMode(String display, boolean visible, boolean performanceManagerMode) {
        this.displayText = display;
        this.visible = visible;
        this.performanceManagerMode = performanceManagerMode;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isPerformanceManagerMode() {
        return performanceManagerMode;
    }

    public boolean isAvailable() {
        return true;
    }

    public String getDisplayText() {
        return displayText;
    }

    public abstract void updateLines(List<UILabel> lines);

    public static void displayMetrics(TObjectDoubleMap<String> metrics, List<UILabel> lines) {
        final List<String> activities = new ArrayList<String>();
        final List<Double> values = new ArrayList<Double>();
        sortMetrics(metrics, activities, values);

        for (int i = 0; i < lines.size() && i < activities.size(); ++i) {
            UILabel line = lines.get(i);
            line.setVisible(true);
            line.setText(String.format("%s: %.2fms", activities.get(i), values.get(i)));
        }
        for (int i = activities.size(); i < lines.size(); ++i) {
            lines.get(i).setVisible(false);
        }
    }

    public static void sortMetrics(TObjectDoubleMap<String> metrics, final List<String> activities, final List<Double> values) {
        metrics.forEachEntry(new TObjectDoubleProcedure<String>() {
            public boolean execute(String s, double v) {
                boolean inserted = false;
                for (int i = 0; i < values.size() && i < UIScreenMetrics.METRIC_LINES; i++) {
                    if (v > values.get(i)) {
                        values.add(i, v);
                        activities.add(i, s);
                        inserted = true;
                        break;
                    }
                }

                if (!inserted && values.size() < UIScreenMetrics.METRIC_LINES) {
                    activities.add(s);
                    values.add(v);
                }
                return true;
            }
        });
    }

}
