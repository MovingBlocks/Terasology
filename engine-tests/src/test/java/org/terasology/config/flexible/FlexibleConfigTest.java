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

import org.junit.Test;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.flexible.validators.RangedNumberValueValidator;

import static org.junit.Assert.assertEquals;

public class FlexibleConfigTest {
    @Test
    public void testGet() throws Exception {
        FlexibleConfig config = new FlexibleConfig();

        ResourceUrn id = new ResourceUrn("engine-tests", "TestSetting");

        FlexibleConfig.Key<Integer> key = config.add(new Setting<>(id, 50,
                new RangedNumberValueValidator<>(0, 100)));

        Setting<Integer> setting = config.get(key);

        assertEquals(50, setting.getValue().intValue());
    }
}