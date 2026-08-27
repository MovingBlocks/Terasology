// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.sun;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.time.WorldTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * See #94: getMoonPhase() is a hook other systems (rendering, gameplay, a future calendar) can key off
 * without each needing their own day-counting logic, based on the same day counter getSunPosAngle()
 * already uses.
 */
public class DefaultCelestialSystemTest {

    private static final float MOON_CYCLE_DAYS = 29.53f;
    private static final float EPSILON = 1e-4f;

    private WorldTime worldTime;
    private DefaultCelestialSystem celestialSystem;

    @BeforeEach
    public void setup() {
        CelestialModel model = mock(CelestialModel.class);
        WorldProvider worldProvider = mock(WorldProvider.class);
        EntityManager entityManager = mock(EntityManager.class);
        worldTime = mock(WorldTime.class);
        when(worldProvider.getTime()).thenReturn(worldTime);

        celestialSystem = new DefaultCelestialSystem(model, worldProvider, entityManager);
    }

    private void setDays(float days) {
        when(worldTime.getDays()).thenReturn(days);
    }

    @Test
    public void newMoonAtDayZero() {
        setDays(0f);
        assertEquals(0f, celestialSystem.getMoonPhase(), EPSILON);
    }

    @Test
    public void fullMoonHalfwayThroughTheCycle() {
        setDays(MOON_CYCLE_DAYS / 2f);
        assertEquals(0.5f, celestialSystem.getMoonPhase(), EPSILON);
    }

    @Test
    public void phaseWrapsAroundAfterACompleteCycle() {
        setDays(MOON_CYCLE_DAYS + MOON_CYCLE_DAYS / 4f);
        assertEquals(0.25f, celestialSystem.getMoonPhase(), EPSILON);
    }

    @Test
    public void phaseWrapsAroundAfterManyCycles() {
        setDays(MOON_CYCLE_DAYS * 100 + MOON_CYCLE_DAYS * 0.75f);
        assertEquals(0.75f, celestialSystem.getMoonPhase(), EPSILON);
    }

    @Test
    public void phaseStaysInRangeWhenSunIsHalted() {
        setDays(1000f);
        celestialSystem.toggleSunHalting(MOON_CYCLE_DAYS / 4f);
        assertEquals(0.25f, celestialSystem.getMoonPhase(), EPSILON);
    }
}
