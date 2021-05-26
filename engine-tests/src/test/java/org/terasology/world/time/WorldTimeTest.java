// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.time;

import org.junit.jupiter.api.Test;
import org.terasology.engine.world.time.WorldTime;
import org.terasology.engine.world.time.WorldTimeEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the world-time related classes
 */
public class WorldTimeTest {

    @Test
    public void testEventMatchesDaily() {

        float[] fracs = new float[]{0.40f, 0.405f, 0.395f, 0.11111f, 0.3950001f, 0.400001f};
        int[] hits = new int[fracs.length];

        long days = 3;

        for (int tick = 0; tick < WorldTime.TICK_EVENTS_PER_DAY * days; tick++) {
            WorldTimeEvent event = new WorldTimeEvent(tick * WorldTime.TICK_EVENT_RATE);

            for (int i = 0; i < fracs.length; i++) {
                if (event.matchesDaily(fracs[i])) {
                    hits[i]++;
                }
            }
        }

        for (int i = 0; i < fracs.length; i++) {
            assertEquals(days, hits[i], "Fraction " + fracs[i] + " was hits " + hits[i] + " times");
        }
    }
}
