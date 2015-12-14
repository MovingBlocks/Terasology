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

import com.google.common.math.LongMath;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.world.WorldComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.time.WorldTime;

import java.math.RoundingMode;

import static org.terasology.world.time.WorldTime.DAY_LENGTH;

/**
 * A base class that fires events at
 * different times of the day.
 */
public class DefaultCelestialSystem extends BaseComponentSystem implements CelestialSystem, UpdateSubscriberSystem {

    private final WorldTime worldTime;

    private long lastUpdate;

    private final CelestialModel model;

    private final EntityManager entityManager;

    public DefaultCelestialSystem(CelestialModel model, Context context) {
        WorldProvider worldProvider = context.get(WorldProvider.class);
        entityManager = context.get(EntityManager.class);
        worldTime = worldProvider.getTime();
        lastUpdate = worldTime.getMilliseconds();
        this.model = model;
    }

    @Override
    public void update(float delta) {
        fireEvents();
    }

    @Override
    public float getSunPosAngle() {
        float days = getWorldTime().getDays();
        float sunPosAngle = model.getSunPosAngle(days);
        return sunPosAngle;
    }

    protected void fireEvents() {
        long startTime = worldTime.getMilliseconds();
        long delta = startTime - lastUpdate;
        if (delta > 0) {
            long timeInDay = LongMath.mod(startTime, DAY_LENGTH);
            long day = LongMath.divide(startTime, DAY_LENGTH, RoundingMode.FLOOR);

            long dawn = model.getDawn(day);
            long midday = model.getMidday(day);
            long dusk = model.getDusk(day);
            long midnight = model.getMidnight(day);

            if (timeInDay - delta < midday && timeInDay >= midday) {
                long tick = day * DAY_LENGTH + midday;
                getWorldEntity().send(new OnMiddayEvent(tick));
            }

            if (timeInDay - delta < dusk && timeInDay >= dusk) {
                long tick = day * DAY_LENGTH + dusk;
                getWorldEntity().send(new OnDuskEvent(tick));
            }

            if (timeInDay - delta < midnight && timeInDay >= midnight) {
                long tick = day * DAY_LENGTH + midnight;
                getWorldEntity().send(new OnMidnightEvent(tick));
            }

            if (timeInDay - delta < dawn && timeInDay >= dawn) {
                long tick = day * DAY_LENGTH + dawn;
                getWorldEntity().send(new OnDawnEvent(tick));
            }
        }

        lastUpdate = startTime;
    }

    protected WorldTime getWorldTime() {
        return worldTime;
    }

    protected EntityRef getWorldEntity() {
        for (EntityRef entity : entityManager.getEntitiesWith(WorldComponent.class)) {
            return entity;
        }
        return EntityRef.NULL;
    }
}
