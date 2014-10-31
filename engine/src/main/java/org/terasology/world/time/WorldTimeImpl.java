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
package org.terasology.world.time;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;

import com.google.common.math.LongMath;

import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Immortius
 */
public class WorldTimeImpl extends BaseComponentSystem implements WorldTime, UpdateSubscriberSystem {

    private static final long DAWN_TIME = DAY_LENGTH;
    private static final long DUSK_TIME = DAY_LENGTH / 2;

    private static final float WORLD_TIME_MULTIPLIER = 48f;

    private AtomicLong worldTime = new AtomicLong(0);

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @Override
    public long getMilliseconds() {
        return worldTime.get();
    }

    @Override
    public float getSeconds() {
        return worldTime.get() / 1000f;
    }

    @Override
    public float getDays() {
        return worldTime.get() / (float) DAY_LENGTH;
    }

    @Override
    public float getTimeRate() {
        return WORLD_TIME_MULTIPLIER;
    }

    @Override
    public void setMilliseconds(long newWorldTime) {
        // TODO: Send network event to update
        this.worldTime.getAndSet(newWorldTime);
    }

    @Override
    public void setDays(float timeInDays) {
        setMilliseconds((long) ((double) timeInDays * DAY_LENGTH));
    }

    @Override
    public void update(float delta) {
        long deltaMs = time.getDeltaInMs();
        if (deltaMs > 0) {
            deltaMs = (long) (deltaMs * WORLD_TIME_MULTIPLIER);
            long startTime = worldTime.getAndAdd(deltaMs);
            long endTime = startTime + deltaMs;
            long timeInDay = LongMath.mod(startTime, DAY_LENGTH);
            long day = LongMath.divide(startTime, DAY_LENGTH, RoundingMode.FLOOR);

            long startTick = startTime / TICK_EVENT_RATE;
            long endTick = (endTime) / TICK_EVENT_RATE;

            if (startTick != endTick) {
                long tick = endTime - endTime % TICK_EVENT_RATE;
                getWorldEntity().send(new WorldTimeEvent(tick));
            }

            if (timeInDay < MIDDAY_TIME && timeInDay + deltaMs >= MIDDAY_TIME) {
                long tick = day * DAY_LENGTH + MIDDAY_TIME;
                getWorldEntity().send(new OnMiddayEvent(tick));
            }

            if (timeInDay < DUSK_TIME && timeInDay + deltaMs >= DUSK_TIME) {
                long tick = day * DAY_LENGTH + DUSK_TIME;
                getWorldEntity().send(new OnDuskEvent(tick));
            }

            if (timeInDay < MIDNIGHT_TIME && timeInDay + deltaMs >= MIDNIGHT_TIME) {
                long tick = day * DAY_LENGTH + MIDNIGHT_TIME;
                getWorldEntity().send(new OnMidnightEvent(tick));
            }

            if (timeInDay < DAWN_TIME && timeInDay + deltaMs >= DAWN_TIME) {
                long tick = day * DAY_LENGTH + DAWN_TIME;
                getWorldEntity().send(new OnDawnEvent(tick));
            }
        }
    }

    private EntityRef getWorldEntity() {
        for (EntityRef entity : entityManager.getEntitiesWith(WorldComponent.class)) {
            return entity;
        }
        return EntityRef.NULL;
    }
}
