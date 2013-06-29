package org.terasology.world.time;

import org.terasology.engine.CoreRegistry;
import org.terasology.engine.Time;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.CoreMessageType;
import org.terasology.world.WorldComponent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Immortius
 */
public class WorldTimeImpl implements WorldTime, UpdateSubscriberSystem {
    private static final float WORLD_TIME_MULTIPLIER = 48f;
    private static final long DAYS_TO_MS = (DAY_LENGTH);
    private static final float MS_TO_DAYS = 1.f / (DAYS_TO_MS);

    private static final long DAWN_TIME = DAY_LENGTH;
    private static final long MIDDAY_TIME = DAY_LENGTH / 4;
    private static final long DUSK_TIME = DAY_LENGTH / 2;
    private static final long MIDNIGHT_TIME = 3 * DAY_LENGTH / 4;

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
    public void setMilliseconds(long time) {
        // TODO: Send network event to update
        this.worldTime.getAndSet(time);
    }

    @Override
    public void setDays(float timeInDays) {
        setMilliseconds((long) ((double) timeInDays * DAYS_TO_MS));
    }

    @Override
    public void update(float delta) {
        long deltaMs = time.getDeltaInMs();
        if (deltaMs > 0) {
            deltaMs = (long)(deltaMs * WORLD_TIME_MULTIPLIER);
            long startTime = worldTime.getAndAdd(deltaMs);
            long timeInDay = startTime % DAY_LENGTH;
            if (timeInDay < 0) {
                timeInDay = DAY_LENGTH + timeInDay;
            }
            if (timeInDay < MIDDAY_TIME) {
                if (timeInDay + deltaMs >= MIDDAY_TIME) {
                    getWorldEntity().send(new OnMiddayEvent());
                }
            } else if (timeInDay < DUSK_TIME) {
                if (timeInDay + deltaMs >= DUSK_TIME) {
                    getWorldEntity().send(new OnDuskEvent());
                }
            } else if (timeInDay < MIDNIGHT_TIME) {
                if (timeInDay + deltaMs >= MIDNIGHT_TIME) {
                    getWorldEntity().send(new OnMidnightEvent());
                }
            } else if (timeInDay < DAWN_TIME) {
                if (timeInDay + deltaMs >= DAWN_TIME) {
                    getWorldEntity().send(new OnDawnEvent());
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
