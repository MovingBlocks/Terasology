// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.sun;

import com.google.common.collect.Iterables;
import com.google.common.math.LongMath;
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
 * A base class that fires events at different times of the day.
 */
public class DefaultCelestialSystem extends BaseComponentSystem implements CelestialSystem, UpdateSubscriberSystem {

    private final WorldTime worldTime;
    private final CelestialModel model;
    private final EntityManager entityManager;
    private long lastUpdate;
    private boolean haltSunPosition = false;

    private float haltedTime = 0f;

    public DefaultCelestialSystem(CelestialModel model, WorldProvider worldProvider, EntityManager entityManager) {
        this.entityManager = entityManager;
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
        float days;
        if (isSunHalted()) {
            days = haltedTime;
        } else {
            days = getWorldTime().getDays();
        }
        return model.getSunPosAngle(days);
    }

    @Override
    public boolean isSunHalted() {
        return haltSunPosition;
    }

    @Override
    public void toggleSunHalting(float timeInDays) {
        haltSunPosition = !haltSunPosition;
        haltedTime = timeInDays;
    }

    /**
     * Updates the game perception of the time of day via launching a new OnMiddayEvent(), OnDuskEvent(),
     * OnMidnightEvent(), or OnDawnEvent() based on the time of day when this method is called upon.
     */
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
        return Iterables.getFirst(
                entityManager.getEntitiesWith(WorldComponent.class),
                EntityRef.NULL);
    }
}
