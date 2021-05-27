// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.ingame.metrics;

import com.google.common.collect.Lists;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TObjectDoubleMap;

import java.text.NumberFormat;
import java.util.List;

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
