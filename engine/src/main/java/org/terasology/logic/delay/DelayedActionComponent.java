/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
public final class DelayedActionComponent implements Component {
    private Map<String, Long> actionIdsWakeUp = new HashMap<>();
    private long lowestWakeUp = Long.MAX_VALUE;

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
}
