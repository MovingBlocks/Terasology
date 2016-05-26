/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.layers.ingame.metrics;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class DebugMetricsSystemTest {

    @Test
    public void testGetCurrentMode() {
        DebugMetricsSystem system = getEmptySystem();
        assertNotNull(system.getCurrentMode());
    }

    @Test
    public void testToggle() {
        DebugMetricsSystem system = new DebugMetricsSystem();
        system.initialise();

        MetricsMode startingMode = system.toggle();

        MetricsMode mode = null;
        for (int i = 1; i < system.getNumberOfModes(); i++) {
            mode = system.toggle();
        }

        assertEquals(startingMode, mode);
    }

    @Test
    public void testEmptySystemToggle() {
        DebugMetricsSystem system = getEmptySystem();
        MetricsMode defaultMode = system.toggle();
        for (int i = 0; i < 10; i++) {
            assertEquals(defaultMode, system.toggle());
        }
    }

    @Test
    public void testGetCurrentModeNotNull() {
        DebugMetricsSystem system = getEmptySystem();

        assertNotNull(system.getCurrentMode());
    }


    @Test
    public void testToggleNotNull() {
        DebugMetricsSystem system = getEmptySystem();

        assertNotNull(system.toggle());
    }


    @Test
    public void testRegister() {
        DebugMetricsSystem system = getEmptySystem();

        boolean result = system.register(new RunningMeansMode());

        assertTrue(result);
        assertEquals(2, system.getNumberOfModes());

        result = system.register(new SpikesMode());
        assertTrue(result);

    }

    @Test
    public void testUnregister() {
        DebugMetricsSystem system = getEmptySystem();

        MetricsMode means = new RunningMeansMode();
        system.register(means);
        assertEquals(2, system.getNumberOfModes());

        system.unregister(means);
        assertEquals(1, system.getNumberOfModes());
    }

    @Test
    public void testUnregisterWithToggle() {
        DebugMetricsSystem system = getEmptySystem();

        List<MetricsMode> modes = Lists.newArrayList(new RunningMeansMode(), new SpikesMode(), new RunningThreadsMode());

        for (MetricsMode mode : modes) {
            system.register(mode);
        }

        assertEquals(4, system.getNumberOfModes());
        assertEquals(NullMetricsMode.class, system.getCurrentMode().getClass());

        for (MetricsMode mode : modes) {
            assertEquals(mode, system.toggle());
        }
        // currentMode = RunningThreadsMode(), index

        assertEquals(modes.get(2), system.getCurrentMode());
        system.unregister(modes.get(2));

        // currentMode must be NullMetricsMode here
        assertEquals(3, system.getNumberOfModes());
        assertEquals(modes.get(0), system.toggle()); // when system is toggled currentMode must be RunningMeansMode

        system.unregister(modes.get(0)); // when RunningMeansNode is removed, next mode SpikesMode must be toggled
        assertEquals(modes.get(1), system.getCurrentMode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemovingDefaultMetric() {
        DebugMetricsSystem system = getEmptySystem();
        MetricsMode defaultMode = system.getCurrentMode();
        system.unregister(defaultMode);
    }

    @Test(expected = NullPointerException.class)
    public void testRegisterNull() {
        DebugMetricsSystem system = getEmptySystem();
        system.register(null);
    }

    @Test(expected = NullPointerException.class)
    public void testUnregisterNull() {
        DebugMetricsSystem system = getEmptySystem();
        system.unregister(null);
    }

    @Test
    public void testUnregisterWithToggleRemovingNext() {
        DebugMetricsSystem system = getEmptySystem();

        List<MetricsMode> modes = Lists.newArrayList(new RunningMeansMode(), new SpikesMode(), new RunningThreadsMode());


        for (MetricsMode mode : modes) {
            system.register(mode);
        }
        assertEquals(NullMetricsMode.class, system.getCurrentMode().getClass());
        assertEquals(RunningMeansMode.class, system.toggle().getClass());

        assertTrue(system.unregister(modes.get(1))); // removing SpikesMode
        assertEquals(RunningMeansMode.class, system.getCurrentMode().getClass());
        assertEquals(RunningThreadsMode.class, system.toggle().getClass());
        assertTrue(system.unregister(modes.get(2)));
        assertEquals(NullMetricsMode.class, system.getCurrentMode().getClass());

        assertEquals(RunningMeansMode.class, system.toggle().getClass());
        assertEquals(NullMetricsMode.class, system.toggle().getClass());
    }

    private DebugMetricsSystem getEmptySystem() {
        DebugMetricsSystem system = new DebugMetricsSystem();
        system.initialise();
        system.unregisterAll();
        return system;
    }
}
