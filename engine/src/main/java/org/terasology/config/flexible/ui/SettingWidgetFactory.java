// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.config.flexible.ui;

import com.google.common.collect.ImmutableMap;
import org.terasology.assets.management.AssetManager;
import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.module.ModuleEnvironment;
import org.terasology.nui.UIWidget;
import org.terasology.registry.In;
import org.terasology.registry.InjectionHelper;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Type;
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
            Type constraintType =
                ReflectionUtil.getTypeParameterForSuper(widgetType, ConstraintWidgetFactory.class, 1);

            if (constraint.getClass().equals(ReflectionUtil.getRawType(constraintType))) {
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
