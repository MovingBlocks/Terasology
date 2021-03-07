// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.config.flexible.ui;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.management.AssetTypeManager;
import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.NumberRangeConstraint;
import org.terasology.module.ModuleEnvironment;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SettingWidgetFactoryTest {
    @Test
     void testCreateWidgetFor() {
        ModuleEnvironment environment = mock(ModuleEnvironment.class);

        when(environment.getSubtypesOf(eq(ConstraintWidgetFactory.class)))
            .thenReturn(Lists.newArrayList(NumberRangeConstraintWidgetFactory.class));

        AssetManager assetManager = new AssetManager(mock(AssetTypeManager.class));

        SettingWidgetFactory settingWidgetFactory = new SettingWidgetFactory(environment, assetManager);

        Setting<Integer> setting = mock(Setting.class);

        when(setting.getConstraint())
            .thenReturn(new NumberRangeConstraint<>(0, 10, false, false));

        Optional<ConstraintWidgetFactory<Integer, ?>> widget = settingWidgetFactory.getConstraintWidgetFactory(setting);

        assertTrue(widget.isPresent());
        assertTrue(widget.get() instanceof NumberRangeConstraintWidgetFactory);
    }
}
