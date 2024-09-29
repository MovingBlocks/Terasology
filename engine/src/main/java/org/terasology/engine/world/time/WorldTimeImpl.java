// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.time;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldComponent;

import java.util.concurrent.atomic.AtomicLong;

public class WorldTimeImpl extends BaseComponentSystem implements WorldTime, UpdateSubscriberSystem {
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
        long deltaMs = time.getGameDeltaInMs();
        if (deltaMs > 0) {
            deltaMs = (long) (deltaMs * WORLD_TIME_MULTIPLIER);
            long startTime = worldTime.getAndAdd(deltaMs);
            long endTime = startTime + deltaMs;

            long startTick = startTime / TICK_EVENT_RATE;
            long endTick = endTime / TICK_EVENT_RATE;

            if (startTick != endTick) {
                long tick = endTime - endTime % TICK_EVENT_RATE;
                for (EntityRef e : entityManager.getEntitiesWith(WorldComponent.class)) {
                    // this may not be called if there's no entity with World Component (yet)
                    // TODO: consider catching / logging this case (someplace better than here)
                    e.send(new WorldTimeEvent(tick));
                }
            }

            // TODO: consider sending a DailyTick (independent from solar events such as midnight)
        }
    }
}
