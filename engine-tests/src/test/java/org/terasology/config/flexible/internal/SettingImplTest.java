// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible.internal;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.terasology.engine.config.flexible.Setting;
import org.terasology.engine.config.flexible.SettingChangeListener;
import org.terasology.engine.config.flexible.constraints.NumberRangeConstraint;
import org.terasology.engine.core.SimpleUri;
import org.terasology.reflection.TypeInfo;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SettingImplTest {
    private static final SimpleUri SETTING_ID = new SimpleUri("engine-tests:TestSetting");

    @Nested
    class SetValue {
        private Setting<Integer> setting;

        private int eventResult;

        @BeforeEach
        public void setUp() {
            setting = new SettingImpl<>(
                    TypeInfo.of(Integer.class), 50,
                    new NumberRangeConstraint<>(0, 100, false, false),
                    "", "", Optional::empty);

            eventResult = -1;

            setting.subscribe((setting1, oldValue) -> eventResult = setting1.get());
        }

        @Test
        void testSetsValue() {
            assertTrue(setting.set(25));

            assertEquals(25, eventResult);
        }

        @Test
        void testDoesNotSetValue() {
            assertFalse(setting.set(101));

            assertEquals(-1, eventResult);
        }
    }

    @Nested
    class Override {
        private static final int TEST_CONFIG_OVERRIDE_VALUE = 75;
        private Integer override;
        private Setting<Integer> setting;

        private int eventResult;

        @BeforeEach
        public void setUp() {
            setting = new SettingImpl<>(
                    TypeInfo.of(Integer.class), 50,
                    new NumberRangeConstraint<>(0, 100, false, false),
                    "", "", () -> Optional.ofNullable(override));

            eventResult = -1;

            setting.subscribe((setting1, oldValue) -> eventResult = setting1.get());
        }

        @Test
        void testSystemPropertyValue() {
            override = TEST_CONFIG_OVERRIDE_VALUE;
            assertEquals(75, setting.get());
        }

        @Test
        void testSystemPropertyValueNotPresent() {
            override = null;
            assertEquals(50, setting.get());
        }

        @Test
        void testDoesNotSetValue() {
            assertFalse(setting.set(101));

            assertEquals(-1, eventResult);
        }
    }

    @Nested
    class Subscribers {
        private Setting<Integer> setting;

        private SettingChangeListener<Integer> listener;

        private int eventCallCount;

        @BeforeEach
        public void setUp() {
            setting = new SettingImpl<>(
                    TypeInfo.of(Integer.class), 50,
                    new NumberRangeConstraint<>(0, 100, false, false),
                    "", "", Optional::empty);

            eventCallCount = 0;

            listener = (setting, oldValue) -> eventCallCount++;
        }

        @Test
        void testHasSubscribers() {
            setting.subscribe(listener);

            assertTrue(setting.hasSubscribers());

            setting.unsubscribe(listener);

            assertFalse(setting.hasSubscribers());
        }

        @Test
        void testSetEventCall() {
            setting.subscribe(listener);

            Random random = new FastRandom();

            final int maxSetValueCount = 50;
            int expectedEventCallCount = 0;

            for (int i = 0; i < maxSetValueCount; i++) {
                int randomInt = random.nextInt(-50, 150);
                expectedEventCallCount += setting.set(randomInt) ? 1 : 0;
            }

            assertEquals(expectedEventCallCount, eventCallCount);
        }

        @Test
        void testSubscribe() {
            final int subscriberCount = 10;

            for (int i = 0; i < subscriberCount; i++) {
                setting.subscribe((setting1, oldValue) -> eventCallCount++);
            }

            setting.set(30);

            assertEquals(subscriberCount, eventCallCount);
        }

        @Test
        void testUnsubscribe() {
            int subscriberCount = 10;

            List<SettingChangeListener<Integer>> listeners = Lists.newArrayListWithCapacity(subscriberCount);

            for (int i = 0; i < subscriberCount; i++) {
                SettingChangeListener<Integer> listener = (setting1, oldValue) -> eventCallCount++;
                listeners.add(listener);
                setting.subscribe(listener);
            }

            for (int i = 0; i < new FastRandom().nextInt(subscriberCount / 2); i++) {
                setting.unsubscribe(listeners.get(i));
                subscriberCount--;
            }

            setting.set(30);

            assertEquals(subscriberCount, eventCallCount);
        }
    }
}
