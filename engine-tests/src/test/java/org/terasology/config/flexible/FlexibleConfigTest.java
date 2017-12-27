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
import org.terasology.config.flexible.settings.Setting;
import org.terasology.engine.SimpleUri;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class FlexibleConfigTest {
    private static final SimpleUri KEY_NON_EXISTENT = new SimpleUri("engine-tests:TestSettingX");

    public static class Get {
        private FlexibleConfig config;

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfigImpl();
        }

        @Test
        public void testGet() throws Exception {
            SimpleUri id1 = new SimpleUri("engine-tests:TestSetting1");
            SimpleUri id2 = new SimpleUri("engine-tests:TestSetting2");

            Setting<Integer> expectedSetting1 = new MockSetting<>(id1);
            config.add(expectedSetting1);

            Setting<Double> expectedSetting2 = new MockSetting<>(id2);
            config.add(expectedSetting2);

            Setting<Integer> retrievedSetting1 = config.get(id1);
            Setting<Double> retrievedSetting2 = config.get(id2);

            // We need the references to be equal
            assertEquals(expectedSetting1, retrievedSetting1);
            assertEquals(expectedSetting2, retrievedSetting2);
        }
    }

    public static class Contains {
        private FlexibleConfig config;

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfigImpl();
        }

        @Test
        public void testContains() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");
            config.add(new MockSetting<Integer>(id));

            assertTrue(config.contains(id));
        }

        @Test
        public void testNotContains() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");
            config.add(new MockSetting<Integer>(id));

            assertFalse(config.contains(KEY_NON_EXISTENT));
        }
    }

    public static class Add {
        private FlexibleConfig config;

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfigImpl();
        }

        @Test
        public void testAdd() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            assertTrue(config.add(new MockSetting(id)));
        }

        @Test
        public void testAddExisting() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");

            assertTrue(config.add(new MockSetting<Integer>(id)));
            assertFalse(config.add(new MockSetting<Double>(id)));
        }
    }

    public static class Remove {
        private FlexibleConfig config;

        @Before
        public void setUp() throws Exception {
            config = new FlexibleConfigImpl();
        }

        @Test
        public void testRemove() throws Exception {
            SimpleUri id1 = new SimpleUri("engine-tests:TestSetting1");
            SimpleUri id2 = new SimpleUri("engine-tests:TestSetting2");

            config.add(new MockSetting(id1));
            config.add(new MockSetting(id2));

            assertTrue(config.remove(id1));
            assertTrue(config.remove(id2));
        }

        @Test
        public void testNonexistentRemove() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");
            config.add(new MockSetting(id));

            assertFalse(config.remove(KEY_NON_EXISTENT));
        }

        @Test
        public void testSubscribedRemove() throws Exception {
            SimpleUri id = new SimpleUri("engine-tests:TestSetting");
            Setting setting = new MockSetting(id);
            config.add(setting);
            setting.subscribe(propertyChangeEvent -> {
            });

            assertFalse(config.remove(id));
        }
    }
}
