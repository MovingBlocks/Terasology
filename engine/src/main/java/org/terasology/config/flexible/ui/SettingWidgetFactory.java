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

import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.module.ModuleEnvironment;
import org.terasology.utilities.ReflectionUtil;

import java.util.Optional;

public class SettingWidgetFactory {
    private final ModuleEnvironment environment;

    public SettingWidgetFactory(ModuleEnvironment environment) {
        this.environment = environment;
    }

    public Optional<SettingWidget<?>> createWidgetFor(Setting<?> setting) {
        SettingConstraint<?> constraint = setting.getConstraint();

        for (Class<? extends SettingWidget> widgetType : environment.getSubtypesOf(SettingWidget.class)) {
            Class<?> constraintType =
                ReflectionUtil.getTypeParameterForSuper(widgetType, SettingWidget.class, 0);

            if (constraint.getClass().equals(constraintType)) {
                try {
                    SettingWidget<?> widget = widgetType.newInstance();

                    return Optional.of(widget);
                } catch (InstantiationException | IllegalAccessException ignored) { }
            }
        }

        return Optional.empty();
    }
}
