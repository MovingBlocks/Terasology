// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.ingame.metrics;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import org.terasology.engine.monitoring.PerformanceMonitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Like other MetricsMode implementations, an instance of this class will output a list of activities registered
 * with the PerformanceMonitor and the execution time running means for each, in milliseconds.
 * <br><br>
 * Differently from other implementations, this class filters out all activities that are not prefixed
 * with the strings "WorldRenderer::" and "PostProcessor::". Furthermore, it orders the activities alphabetically
 * and poses no limit on the number of items displayed. Finally it places the time value on the left, making an
 * attempt to align the digits, while the name of the associated activity is on the right, after a separator.
 */
public class RenderingExecTimeMeansMode extends MetricsMode {

    private StringBuilder builder = new StringBuilder();
    private ArrayList<MetricsEntry> processedEntries = new ArrayList<>();
    private AlphabeticalAscendingComparator inAscendingAlphabeticalOrder = new AlphabeticalAscendingComparator();

    private TObjectDoubleProcedure<String> matchingCriteria = new FilterByStartWith();
    private EntryAdder addEntryToProcessedEntries = new EntryAdder();

    public RenderingExecTimeMeansMode(String name) {
        super(name);
    }

    @Override
    public String getMetrics() {
        builder.setLength(0);
        builder.append(getName());
        builder.append("\n");

        processMetrics(PerformanceMonitor.getRunningMean());

        return builder.toString();
    }

    private void processMetrics(TObjectDoubleMap<String> activitiesToMetricsMap) {
        activitiesToMetricsMap.retainEntries(matchingCriteria);

        processedEntries.clear();
        activitiesToMetricsMap.forEachEntry(addEntryToProcessedEntries);
        Collections.sort(processedEntries, inAscendingAlphabeticalOrder);

        for (MetricsEntry entry : processedEntries) {
            builder.append(String.format("%,10.2f", entry.metricsValue));
            builder.append("ms - ");
            builder.append(entry.activityName);
            builder.append("\n");
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isPerformanceManagerMode() {
        return true;
    }

    private class FilterByStartWith implements TObjectDoubleProcedure<String> {
        @Override
        public boolean execute(String activityName, double metricsValue) {
            return activityName.startsWith("WorldRenderer::") || activityName.startsWith("PostProcessor::");
        }
    }

    private class MetricsEntry {
        public String activityName;
        public double metricsValue;

         MetricsEntry(String activityName, double metricsValue) {
            this.activityName = activityName;
            this.metricsValue = metricsValue;
        }
    }

    private class EntryAdder implements TObjectDoubleProcedure<String> {
        @Override
        public boolean execute(String activityName, double metricsValue) {
            processedEntries.add(new MetricsEntry(activityName, metricsValue));
            return true;
        }
    }

    private static class AlphabeticalAscendingComparator implements Comparator<MetricsEntry> {
        @Override
        public int compare(MetricsEntry firstEntry, MetricsEntry secondEntry) {
            return firstEntry.activityName.compareTo(secondEntry.activityName);
        }
    }
}
