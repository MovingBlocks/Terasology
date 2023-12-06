// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.delay;

import com.google.common.collect.Maps;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Not for public use. Use DelayManager instead.
 */
@ForceBlockActive
public final class PeriodicActionComponent implements Component<PeriodicActionComponent> {
    public Map<String, Long> actionIdsWakeUp = new HashMap<>();
    public Map<String, Long> actionIdsPeriod = new HashMap<>();
    public long lowestWakeUp = Long.MAX_VALUE;

    public PeriodicActionComponent() {
    }

    public void addScheduledActionId(String actionId, long wakeUp, long period) {
        actionIdsWakeUp.put(actionId, wakeUp);
        actionIdsPeriod.put(actionId, period);
        lowestWakeUp = Math.min(lowestWakeUp, wakeUp);
    }

    public void removeScheduledActionId(String actionId) {
        final Long removedWakeUp = actionIdsWakeUp.remove(actionId);
        actionIdsPeriod.remove(actionId);
        if (removedWakeUp == lowestWakeUp) {
            lowestWakeUp = findSmallestWakeUp();
        }
    }

    public Set<String> getTriggeredActionsAndReschedule(final long worldTime) {
        final Set<String> result = new HashSet<>();
        final Iterator<Map.Entry<String, Long>> entryIterator = actionIdsWakeUp.entrySet().iterator();
        while (entryIterator.hasNext()) {
            final Map.Entry<String, Long> entry = entryIterator.next();
            if (entry.getValue() <= worldTime) {
                result.add(entry.getKey());
                entryIterator.remove();
            }
        }

        // Rescheduling
        for (String actionId : result) {
            actionIdsWakeUp.put(actionId, worldTime + actionIdsPeriod.get(actionId));
        }

        lowestWakeUp = findSmallestWakeUp();

        return result;
    }

    public long getLowestWakeUp() {
        return lowestWakeUp;
    }

    private long findSmallestWakeUp() {
        long result = Long.MAX_VALUE;
        for (long value : actionIdsWakeUp.values()) {
            result = Math.min(result, value);
        }
        return result;
    }

    public boolean isEmpty() {
        return actionIdsWakeUp.isEmpty();
    }

    public boolean containsActionId(String actionId) {
        return actionIdsWakeUp.containsKey(actionId);
    }

    @Override
    public void copyFrom(PeriodicActionComponent other) {
        this.actionIdsPeriod = Maps.newHashMap(other.actionIdsPeriod);
        this.actionIdsWakeUp = Maps.newHashMap(other.actionIdsPeriod);
        this.lowestWakeUp = other.lowestWakeUp;
    }
}
