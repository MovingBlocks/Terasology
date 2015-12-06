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
package org.terasology.logic.delay;

import org.terasology.entitySystem.Component;
import org.terasology.world.block.ForceBlockActive;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Not for public use. Use DelayManager instead.
 */
@ForceBlockActive
public final class PeriodicActionComponent implements Component {
    private Map<String, Long> actionIdsWakeUp = new HashMap<>();
    private Map<String, Long> actionIdsPeriod = new HashMap<>();
    private long lowestWakeUp = Long.MAX_VALUE;

    public PeriodicActionComponent() {
    }

    public void addScheduledActionId(String actionId, long wakeUp, long period) {
        actionIdsWakeUp.put(actionId, wakeUp);
        actionIdsPeriod.put(actionId, period);
        lowestWakeUp = Math.min(lowestWakeUp, wakeUp);
    }

    public void removeScheduledActionId(String actionId) {
        final long removedWakeUp = actionIdsWakeUp.remove(actionId);
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
}
