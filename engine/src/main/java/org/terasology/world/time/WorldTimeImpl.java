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
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.world.WorldComponent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Immortius
 */
public class WorldTimeImpl implements WorldTime, UpdateSubscriberSystem {
    public static final long DAYS_TO_MS = (DAY_LENGTH);
    public static final float MS_TO_DAYS = 1.f / (DAYS_TO_MS);

    public static final long DAWN_TIME = DAY_LENGTH;
    public static final long MIDDAY_TIME = DAY_LENGTH / 4;
    public static final long DUSK_TIME = DAY_LENGTH / 2;
    public static final long MIDNIGHT_TIME = 3 * DAY_LENGTH / 4;
    private static final float WORLD_TIME_MULTIPLIER = 48f;

    private AtomicLong worldTime = new AtomicLong(0);

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }


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
        return MS_TO_DAYS * worldTime.get();
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
        setMilliseconds((long) ((double) timeInDays * DAYS_TO_MS));
    }

    @Override
    public void update(float delta) {
        long deltaMs = time.getDeltaInMs();
        if (deltaMs > 0) {
            deltaMs = (long) (deltaMs * WORLD_TIME_MULTIPLIER);
            long startTime = worldTime.getAndAdd(deltaMs);
            long endTime = startTime + deltaMs;
            long timeInDay = startTime % DAY_LENGTH;
            if (timeInDay < 0) {
                timeInDay = DAY_LENGTH + timeInDay;
            }
            if (timeInDay < MIDDAY_TIME) {
                if (timeInDay + deltaMs >= MIDDAY_TIME) {
                    getWorldEntity().send(new OnMiddayEvent(MS_TO_DAYS * endTime, endTime));
                }
            } else if (timeInDay < DUSK_TIME) {
                if (timeInDay + deltaMs >= DUSK_TIME) {
                    getWorldEntity().send(new OnDuskEvent(MS_TO_DAYS * endTime, endTime));
                }
            } else if (timeInDay < MIDNIGHT_TIME) {
                if (timeInDay + deltaMs >= MIDNIGHT_TIME) {
                    getWorldEntity().send(new OnMidnightEvent(MS_TO_DAYS * endTime, endTime));
                }
            } else if (timeInDay < DAWN_TIME) {
                if (timeInDay + deltaMs >= DAWN_TIME) {
                    getWorldEntity().send(new OnDawnEvent(MS_TO_DAYS * endTime, endTime));
                }
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
