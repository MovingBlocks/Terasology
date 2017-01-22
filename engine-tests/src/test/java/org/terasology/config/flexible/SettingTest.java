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
package org.terasology.config.flexible;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.terasology.config.flexible.validators.RangedNumberValidator;
import org.terasology.engine.SimpleUri;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.beans.PropertyChangeListener;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class SettingTest {
    public static class SetValue {
        private Setting<Integer> setting;

        private int eventResult;

        @Before
        public void setUp() {
            setting = new Setting<>(new SimpleUri("engine-tests:TestSetting"),
                    50, new RangedNumberValidator<>(0, 100));

            eventResult = -1;

            setting.subscribe(propertyChangeEvent -> eventResult = (int) propertyChangeEvent.getNewValue());
        }

        @Test
        public void testSetsValue() {
            assertTrue(setting.setValue(25));

            assertEquals(25, eventResult);
        }

        @Test
        public void testDoesNotSetValue() {
            assertFalse(setting.setValue(101));

            assertEquals(-1, eventResult);
        }
    }

    public static class Subscribers {
        private Setting<Integer> setting;

        private PropertyChangeListener listener;

        private int eventCallCount;

        @Before
        public void setUp() {
            setting = new Setting<>(new SimpleUri("engine-tests:TestSetting"),
                    50, new RangedNumberValidator<>(0, 100));

            eventCallCount = 0;

            listener = propertyChangeEvent -> eventCallCount++;
        }

        @Test
        public void testHasSubscribers() {
            setting.subscribe(listener);

            assertTrue(setting.hasSubscribers());

            setting.unsubscribe(listener);

            assertFalse(setting.hasSubscribers());
        }

        @Test
        public void testSetEventCall() {
            setting.subscribe(listener);

            Random random = new FastRandom();

            final int n = 50;
            int expectedEventCallCount = 0;

            for (int i = 0; i < n; i++) {
                int r = random.nextInt(-50, 150);
                expectedEventCallCount += setting.setValue(r) ? 1 : 0;
            }

            assertEquals(expectedEventCallCount, eventCallCount);
        }

        @Test
        public void testSubscribe() {
            final int n = 10;

            for (int i = 0; i < n; i++) {
                setting.subscribe(listener);
            }

            setting.setValue(30);

            assertEquals(n, eventCallCount);
        }

        @Test
        public void testUnsubscribe() {
            int n = 10;

            for (int i = 0; i < n; i++) {
                setting.subscribe(listener);
            }

            int halfN = n / 2;

            for (int i = 0; i < new FastRandom().nextInt(halfN); i++) {
                setting.unsubscribe(listener);
                n--;
            }

            setting.setValue(30);

            assertEquals(n, eventCallCount);
        }
    }
}
