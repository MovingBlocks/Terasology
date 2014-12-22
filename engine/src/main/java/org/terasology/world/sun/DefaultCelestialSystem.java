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

package org.terasology.world.sun;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.WorldComponent;

/**
 * A base class that fires events at
 * different times of the day.
 * @author Martin Steiger
 */
public class DefaultCelestialSystem extends BaseComponentSystem implements CelestialSystem, UpdateSubscriberSystem {

    private final Time time;

    private long lastUpdate;

    private final CelestialModel model;

    public DefaultCelestialSystem(CelestialModel model) {
        time = CoreRegistry.get(Time.class);
        lastUpdate = time.getGameTimeInMs();
        this.model = model;
    }

    @Override
    public void update(float delta) {
        fireEvents();
    }

    @Override
    public CelestialModel getWorldCelestialModel() {
        return model;
    }

    protected void fireEvents() {
        long startTime = time.getGameTimeInMs();
        long delta = startTime - lastUpdate;
        if (delta > 0) {
            float currentDay = model.getDay(startTime);
            long currentDayNumber = (long) currentDay;
            float timeInCurrentDay = currentDay - currentDayNumber;

            float lastUpdateDay = model.getDay(lastUpdate);
            long lastUpdateDayNumber = (long) lastUpdateDay;
            float timeInLastUpdateDay = lastUpdateDay - lastUpdateDayNumber;

            float dawn = model.getDawn(currentDayNumber);
            float midday = model.getMidday(currentDayNumber);
            float dusk = model.getDusk(currentDayNumber);

            if (timeInLastUpdateDay < midday && timeInCurrentDay >= midday) {
                getWorldEntity().send(new OnMiddayEvent(currentDay));
            }

            if (timeInLastUpdateDay < dusk && timeInCurrentDay >= dusk) {
                getWorldEntity().send(new OnDuskEvent(currentDay));
            }

            if (lastUpdateDayNumber < currentDayNumber) {
                getWorldEntity().send(new OnMidnightEvent(currentDay));
            }

            if (timeInLastUpdateDay < dawn && timeInCurrentDay >= dawn) {
                getWorldEntity().send(new OnDawnEvent(currentDay));
            }
        }

        lastUpdate = startTime;
    }

    private EntityRef getWorldEntity() {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef entity : entityManager.getEntitiesWith(WorldComponent.class)) {
            return entity;
        }
        return EntityRef.NULL;
    }
}
