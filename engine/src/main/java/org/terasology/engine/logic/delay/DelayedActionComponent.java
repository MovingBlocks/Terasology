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
public final class DelayedActionComponent implements Component<DelayedActionComponent> {
    public Map<String, Long> actionIdsWakeUp = new HashMap<>();
    public long lowestWakeUp = Long.MAX_VALUE;

    public DelayedActionComponent() {
    }

    public void addActionId(String actionId, long wakeUp) {
        actionIdsWakeUp.put(actionId, wakeUp);
        lowestWakeUp = Math.min(lowestWakeUp, wakeUp);
    }

    public void removeActionId(String actionId) {
        final long removedWakeUp = actionIdsWakeUp.remove(actionId);
        if (removedWakeUp == lowestWakeUp) {
            lowestWakeUp = findSmallestWakeUp();
        }
    }

    public Set<String> removeActionsUpTo(final long worldTime) {
        final Set<String> result = new HashSet<>();
        final Iterator<Map.Entry<String, Long>> entryIterator = actionIdsWakeUp.entrySet().iterator();
        while (entryIterator.hasNext()) {
            final Map.Entry<String, Long> entry = entryIterator.next();
            if (entry.getValue() <= worldTime) {
                result.add(entry.getKey());
                entryIterator.remove();
            }
        }
        lowestWakeUp = findSmallestWakeUp();

        return result;
    }

    public long getLowestWakeUp() {
        return lowestWakeUp;
    }

    public boolean isEmpty() {
        return actionIdsWakeUp.isEmpty();
    }

    public boolean containsActionId(String actionId) {
        return actionIdsWakeUp.containsKey(actionId);
    }

    private long findSmallestWakeUp() {
        long result = Long.MAX_VALUE;
        for (long value : actionIdsWakeUp.values()) {
            result = Math.min(result, value);
        }
        return result;
    }

    @Override
    public void copyFrom(DelayedActionComponent other) {
        this.actionIdsWakeUp = Maps.newHashMap(other.actionIdsWakeUp);
        this.lowestWakeUp = other.lowestWakeUp;
    }
}
