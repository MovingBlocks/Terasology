// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.sun;

import com.google.common.math.LongMath;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.world.time.WorldTime;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.WorldProvider;

import javax.inject.Inject;
import java.math.RoundingMode;

/**
 * A base class that fires events at different times of the day.
 */
public class DefaultCelestialSystem extends BaseComponentSystem implements CelestialSystem, UpdateSubscriberSystem {

    /**
     * Length of a full lunar cycle, in game days. Defaults to the real-world synodic month so a
     * calendar built on top of this lines up with familiar phase names/timing.
     */
    private static final float MOON_CYCLE_DAYS = 29.53f;

    private final WorldTime worldTime;

    private long lastUpdate;

    private final CelestialModel model;

    private final EntityManager entityManager;

    private boolean haltSunPosition = false;

    private float haltedTime = 0f;

    public DefaultCelestialSystem(CelestialModel model, Context context) {
        WorldProvider worldProvider = context.get(WorldProvider.class);
        entityManager = context.get(EntityManager.class);
        worldTime = worldProvider.getTime();
        lastUpdate = worldTime.getMilliseconds();
        this.model = model;
    }

    @Inject
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
        return model.getSunPosAngle(getCurrentDays());
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

    @Override
    public float getMoonPhase() {
        float phase = getCurrentDays() % MOON_CYCLE_DAYS / MOON_CYCLE_DAYS;
        // getCurrentDays() is expected to always be non-negative, but guard against a negative
        // result from a halted/rewound time anyway rather than return an out-of-range phase.
        return phase < 0 ? phase + 1 : phase;
    }

    private float getCurrentDays() {
        return isSunHalted() ? haltedTime : getWorldTime().getDays();
    }
    /**
     * Updates the game perception of the time of day via launching a new OnMiddayEvent(),
     * OnDuskEvent(), OnMidnightEvent(), or OnDawnEvent() based on the time of day when
     * this method is called upon.
     */
    protected void fireEvents() {
        long startTime = worldTime.getMilliseconds();
        long delta = startTime - lastUpdate;
        if (delta > 0) {
            long timeInDay = LongMath.mod(startTime, WorldTime.DAY_LENGTH);
            long day = LongMath.divide(startTime, WorldTime.DAY_LENGTH, RoundingMode.FLOOR);

            long dawn = model.getDawn(day);
            long midday = model.getMidday(day);
            long dusk = model.getDusk(day);
            long midnight = model.getMidnight(day);

            if (timeInDay - delta < midday && timeInDay >= midday) {
                long tick = day * WorldTime.DAY_LENGTH + midday;
                getWorldEntity().send(new OnMiddayEvent(tick));
            }

            if (timeInDay - delta < dusk && timeInDay >= dusk) {
                long tick = day * WorldTime.DAY_LENGTH + dusk;
                getWorldEntity().send(new OnDuskEvent(tick));
            }

            if (timeInDay - delta < midnight && timeInDay >= midnight) {
                long tick = day * WorldTime.DAY_LENGTH + midnight;
                getWorldEntity().send(new OnMidnightEvent(tick));
            }

            if (timeInDay - delta < dawn && timeInDay >= dawn) {
                long tick = day * WorldTime.DAY_LENGTH + dawn;
                getWorldEntity().send(new OnDawnEvent(tick));
            }
        }

        lastUpdate = startTime;
    }

    protected WorldTime getWorldTime() {
        return worldTime;
    }

    @SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
    protected EntityRef getWorldEntity() {
        for (EntityRef entity : entityManager.getEntitiesWith(WorldComponent.class)) {
            return entity;
        }
        return EntityRef.NULL;
    }
}
