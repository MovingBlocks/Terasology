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

import gnu.trove.TCollections;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.procedure.TObjectLongProcedure;
import org.terasology.entitySystem.Component;
import org.terasology.world.block.ForceBlockActive;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@ForceBlockActive
public final class DelayedActionComponent implements Component {
    private TObjectLongMap<String> actionIdsWakeUp = new TObjectLongHashMap<>();
    private long lowestWakeUp = Long.MAX_VALUE;

    public DelayedActionComponent() {
    }

    public void addActionId(String actionId, long wakeUp) {
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
        actionIdsWakeUp.forEachEntry(
                new TObjectLongProcedure<String>() {
                    @Override
                    public boolean execute(String actionId, long time) {
                        if (time <= worldTime) {
                            result.add(actionId);
                        }
                        return true;
                    }
                });
        for (String actionId : result) {
            actionIdsWakeUp.remove(actionId);
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

    public TObjectLongMap<String> getActionIdsWakeUp() {
        return TCollections.unmodifiableMap(actionIdsWakeUp);
    }
}
