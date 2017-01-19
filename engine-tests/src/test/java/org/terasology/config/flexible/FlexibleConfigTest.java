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
import org.terasology.assets.ResourceUrn;
import org.terasology.config.flexible.validators.RangedNumberValueValidator;
import org.terasology.engine.SimpleUri;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class FlexibleConfigTest {
    public static class GetTest {
        private FlexibleConfig config;

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfig();
        }

        @Test
        public void testGet() throws Exception {
            SimpleUri id1 = new SimpleUri("engine-tests:TestSetting1");
            SimpleUri id2 = new SimpleUri("engine-tests:TestSetting2");

            FlexibleConfig.Key<Integer> key1 = config.add(new Setting<>(id1, 50,
                    new RangedNumberValueValidator<>(0, 100)));

            FlexibleConfig.Key<Double> key2 = config.add(new Setting<>(id2, 25d,
                    new RangedNumberValueValidator<>(0d, 100d)));

            assertEquals(50, config.get(key1).getValue().intValue());
            assertEquals(25d, config.get(key2).getValue(), 0.00001d);
        }

        @Test
        public void testGetInvalidType() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            config.add(new Setting<>(id, 50,
                    new RangedNumberValueValidator<>(0, 100)));

            FlexibleConfig.Key<Double> key = new FlexibleConfig.Key<>(id, Double.class);

            Setting<Double> setting = config.get(key);

            assertNull(setting);
        }
    }

    public static class HasTest {
        private FlexibleConfig config;

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfig();
        }

        @Test
        public void testHas() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            FlexibleConfig.Key<Integer> key = config.add(new Setting<>(id, 50,
                    new RangedNumberValueValidator<>(0, 100)));

            assertTrue(config.has(key));
        }

        @Test
        public void testNotHas() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            config.add(new Setting<>(id, 50,
                    new RangedNumberValueValidator<>(0, 100)));

            FlexibleConfig.Key<Integer> key = new FlexibleConfig.Key<>(new SimpleUri("engine-tests:TestSettingX"),
                    Integer.class);

            assertFalse(config.has(key));
        }
    }

    public static class AddTest {
        private FlexibleConfig config;

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfig();
        }

        @Test
        public void testAdd() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            assertNotNull(config.add(new Setting<>(id, 50,
                    new RangedNumberValueValidator<>(0, 100))));
        }

        @Test
        public void testAddExisting() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            assertNotNull(config.add(new Setting<>(id, 50,
                    new RangedNumberValueValidator<>(0, 100))));

            assertNull(config.add(new Setting<>(id, 5d,
                    new RangedNumberValueValidator<>(1d, 100d))));
        }
    }

    public static class RemoveTest {
        private FlexibleConfig config;

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfig();
        }

        @Test
        public void testRemove() throws Exception {
            SimpleUri id1 = new SimpleUri("engine-tests:TestSetting1");
            SimpleUri id2 = new SimpleUri("engine-tests:TestSetting2");

            FlexibleConfig.Key<Integer> key1 = config.add(new Setting<>(id1, 50,
                    new RangedNumberValueValidator<>(0, 100)));

            FlexibleConfig.Key<Double> key2 = config.add(new Setting<>(id2, 25d,
                    new RangedNumberValueValidator<>(0d, 100d)));

            assertTrue(config.remove(key1));
            assertTrue(config.remove(key2));
        }

        @Test
        public void testNonexistentRemove() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            config.add(new Setting<>(id, 50,
                    new RangedNumberValueValidator<>(0, 100)));

            FlexibleConfig.Key<Integer> key = new FlexibleConfig.Key<>(new SimpleUri("engine-tests:TestSettingX"),
                    Integer.class);

            assertFalse(config.remove(key));
        }

        @Test
        public void testSubscribedRemove() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            FlexibleConfig.Key<Integer> key = config.add(new Setting<>(id, 50,
                    new RangedNumberValueValidator<>(0, 100)));

            Setting<Integer> setting = config.get(key);

            setting.subscribe(propertyChangeEvent -> {});

            assertFalse(config.remove(key));
        }
    }
}