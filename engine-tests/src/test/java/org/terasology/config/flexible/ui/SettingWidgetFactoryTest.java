/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.config.flexible.ui;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.NumberRangeConstraint;
import org.terasology.module.ModuleEnvironment;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SettingWidgetFactoryTest {
    @Test
    public void testGetWidgetFor() {
        ModuleEnvironment environment = mock(ModuleEnvironment.class);

        when(environment.getSubtypesOf(eq(SettingWidget.class)))
            .thenReturn(Lists.newArrayList(NumberRangeSettingWidget.class));

        SettingWidgetFactory settingWidgetFactory = new SettingWidgetFactory(environment);

        Setting<Integer> setting = mock(Setting.class);

        when(setting.getConstraint())
            .thenReturn(new NumberRangeConstraint<>(0, 10, false, false));

        Optional<SettingWidget<Integer, ?>> widget = settingWidgetFactory.createWidgetFor(setting);

        assertTrue(widget.isPresent());
        assertTrue(widget.get() instanceof NumberRangeSettingWidget);
    }
}