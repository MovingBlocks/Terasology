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

import com.google.common.collect.ImmutableMap;
import org.terasology.assets.management.AssetManager;
import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.module.ModuleEnvironment;
import org.terasology.registry.In;
import org.terasology.registry.InjectionHelper;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.utilities.ReflectionUtil;

import java.util.Optional;

public class SettingWidgetFactory {
    private final ModuleEnvironment environment;
    private final AssetManager assetManager;

    public SettingWidgetFactory(ModuleEnvironment environment, AssetManager assetManager) {
        this.environment = environment;
        this.assetManager = assetManager;
    }

    public <T> Optional<UIWidget> createWidgetFor(Setting<T> setting) {
        return getConstraintWidgetFactory(setting)
            .flatMap(factory -> factory.buildWidgetFor(setting));
    }

    <T> Optional<ConstraintWidgetFactory<T, ?>> getConstraintWidgetFactory(Setting<T> setting) {
        SettingConstraint<?> constraint = setting.getConstraint();

        for (Class<? extends ConstraintWidgetFactory> widgetType : environment.getSubtypesOf(ConstraintWidgetFactory.class)) {
            Class<?> constraintType =
                ReflectionUtil.getTypeParameterForSuper(widgetType, ConstraintWidgetFactory.class, 1);

            if (constraint.getClass().equals(constraintType)) {
                try {
                    ConstraintWidgetFactory<T, ?> factory = widgetType.newInstance();
                    InjectionHelper.inject(factory, In.class, ImmutableMap.of(AssetManager.class, assetManager));

                    return Optional.of(factory);
                } catch (InstantiationException | IllegalAccessException ignored) { }
            }
        }

        return Optional.empty();

    }
}
