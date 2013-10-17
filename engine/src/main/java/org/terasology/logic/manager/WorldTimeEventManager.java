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
package org.terasology.logic.manager;

import org.terasology.world.WorldProvider;
import org.terasology.world.WorldTimeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
// TODO: merge into WorldTime
public class WorldTimeEventManager {

    protected final List<WorldTimeEvent> worldTimeEvents = new ArrayList<>();
    protected final WorldProvider parent;

    public WorldTimeEventManager(WorldProvider parent) {
        this.parent = parent;
    }

    /**
     * Adds a time event to the list.
     *
     * @param e The time event
     */
    public void addWorldTimeEvent(WorldTimeEvent e) {
        worldTimeEvents.add(e);
    }

    /**
     * Removes a time event from the list.
     *
     * @param e The time event
     */
    public void removeWorldTimeEvent(WorldTimeEvent e) {
        worldTimeEvents.remove(e);
    }

    /**
     * Executes all time events which event times equal a specified delta value.
     */
    public void fireWorldTimeEvents() {
        for (int i = worldTimeEvents.size() - 1; i >= 0; i--) {
            final WorldTimeEvent event = worldTimeEvents.get(i);

            if (event.getExecutionTime() > parent.getTime().getDays() % 1.0) {
                event.setCanFire(true);
            }

            if (event.getExecutionTime() <= parent.getTime().getDays() % 1.0 && event.canFire()) {
                event.setCanFire(false);
                event.execute();
            }

            if (!event.isRepeatingEvent()) {
                worldTimeEvents.remove(i);
            }

        }
    }

}
