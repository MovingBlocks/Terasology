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
    private static final SimpleUri SETTING_ID = new SimpleUri("engine-tests:TestSetting");

    public static class SetValue {
        private Setting<Integer> setting;

        private int eventResult;

        @Before
        public void setUp() {
            setting = new SettingImpl<>(SETTING_ID,
                    50, new RangedNumberValidator<>(0, 100, false, false));

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
            setting = new SettingImpl<>(SETTING_ID,
                    50, new RangedNumberValidator<>(0, 100, false, false));

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

            final int maxSetValueCount = 50;
            int expectedEventCallCount = 0;

            for (int i = 0; i < maxSetValueCount; i++) {
                int randomInt = random.nextInt(-50, 150);
                expectedEventCallCount += setting.setValue(randomInt) ? 1 : 0;
            }

            assertEquals(expectedEventCallCount, eventCallCount);
        }

        @Test
        public void testSubscribe() {
            final int subscriberCount = 10;

            for (int i = 0; i < subscriberCount; i++) {
                setting.subscribe(propertyChangeEvent -> eventCallCount++);
            }

            setting.setValue(30);

            assertEquals(subscriberCount, eventCallCount);
        }

        @Test
        public void testUnsubscribe() {
            int subscriberCount = 10;

            PropertyChangeListener[] listeners = new PropertyChangeListener[subscriberCount];

            for (int i = 0; i < subscriberCount; i++) {
                listeners[i] = propertyChangeEvent -> eventCallCount++;
                setting.subscribe(listeners[i]);
            }

            for (int i = 0; i < new FastRandom().nextInt(subscriberCount / 2); i++) {
                setting.unsubscribe(listeners[i]);
                subscriberCount--;
            }

            setting.setValue(30);

            assertEquals(subscriberCount, eventCallCount);
        }
    }
}
