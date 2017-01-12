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
package org.terasology.config.flexible.setting;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.flexible.setting.Setting;
import org.terasology.config.flexible.setting.validators.RangedNumberValueValidator;
import org.terasology.persistence.typeHandling.coreTypes.DoubleTypeHandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@RunWith(Enclosed.class)
public class SettingTest {
    public static class SetValue {
        private Setting<Integer> setting;

        private int eventResult;

        @Before
        public void setUp() {
            setting = new Setting<>(new ResourceUrn("engine-tests", "TestSetting"),
                    50, new RangedNumberValueValidator<>(0, 100));

            eventResult = -1;

            setting.subscribe(propertyChangeEvent -> eventResult = (int) propertyChangeEvent.getNewValue());
        }

        @Test
        public void testSetsValue() {
            assertTrue(setting.setValue(25));

            assertEquals(25, eventResult);
        }

        @Test
        public void testNotSetsValue() {
            assertFalse(setting.setValue(101));

            assertEquals(-1, eventResult);
        }
    }
}