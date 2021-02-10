/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layers.ingame.metrics;

import com.google.common.collect.Lists;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TObjectDoubleMap;

import java.text.NumberFormat;
import java.util.List;

/**
 */
public abstract class TimeMetricsMode extends MetricsMode {

    private int limit;
    private NumberFormat format;
    private String unit = "ms";


    public TimeMetricsMode(String name, int limit) {
        super(name);
        this.limit = limit;
        format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
    }

    public TimeMetricsMode(String name, int limit, String unit) {
        this(name, limit);
        this.limit = limit;
    }

    @Override
    public String getMetrics() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        builder.append("\n");
        displayMetrics(gatherMetrics(), builder);
        return builder.toString();
    }

    protected abstract TObjectDoubleMap<String> gatherMetrics();

    private void displayMetrics(TObjectDoubleMap<String> metrics, StringBuilder builder) {
        final List<String> activities = Lists.newArrayList();
        final TDoubleList values = new TDoubleArrayList();
        sortMetrics(metrics, activities, values);

        for (int i = 0; i < limit && i < activities.size(); ++i) {
            builder.append(activities.get(i));
            builder.append(": ");
            builder.append(format.format(values.get(i)));
            builder.append(unit);
            builder.append("\n");
        }
    }

    private void sortMetrics(TObjectDoubleMap<String> metrics, final List<String> activities, final TDoubleList values) {
        metrics.forEachEntry((s, v) -> {
            boolean inserted = false;
            for (int i = 0; i < values.size() && i < limit; i++) {
                if (v > values.get(i)) {
                    values.insert(i, v);
                    activities.add(i, s);
                    inserted = true;
                    break;
                }
            }

            if (!inserted && values.size() < limit) {
                activities.add(s);
                values.add(v);
            }
            return true;
        });
    }
}
