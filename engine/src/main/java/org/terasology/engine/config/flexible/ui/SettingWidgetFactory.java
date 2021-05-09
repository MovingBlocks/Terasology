// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible.ui;

import com.google.common.collect.ImmutableMap;
import org.terasology.engine.config.flexible.Setting;
import org.terasology.engine.config.flexible.constraints.SettingConstraint;
import org.terasology.engine.context.Context;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.engine.utilities.ReflectionUtil;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.nui.UIWidget;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Creates {@link UIWidget} for {@link Setting}.
 * <p>
 * Created {@link UIWidget}'s binded with source {@link Setting}. Changing values in {@link UIWidget} will change
 * value in related {@link Setting}
 * <p>
 * You can extends functionality via implementing {@link ConstraintWidgetFactory} for your custom type.
 * Place your custom class somewhere in classpath. {@link SettingWidgetFactory} will find your implementation.
 * <p>
 * Useful for creating settings UI.
 */
public class SettingWidgetFactory {
    private final ModuleEnvironment environment;
    private final AssetManager assetManager;
    private final Context context;

    public SettingWidgetFactory(ModuleEnvironment environment, AssetManager assetManager, Context context) {
        this.environment = environment;
        this.assetManager = assetManager;
        this.context = context;
    }

    /**
     * Try to create {@link UIWidget} for {@link Setting}
     * @param setting for widget.
     * @param <T> type of setting
     * @return {@link UIWidget} for {@link Setting} if success, {@code Optinal.empty()} otherwise.
     */
    public <T> Optional<UIWidget> createWidgetFor(Setting<T> setting) {
        return getConstraintWidgetFactory(setting)
                .orElseGet(()-> new DefaultConstraintWidgetFactory<>(context))
                .buildWidgetFor(setting);
    }

    /**
     * Try to find {@link ConstraintWidgetFactory} for this {@link Setting}.
     *
     * @param setting for constaint factory.
     * @param <T> type of settings
     * @return {@link ConstraintWidgetFactory} for {@link Setting} if found, {@code Optinal.empty()} otherwise.
     */
    <T> Optional<ConstraintWidgetFactory<T, ?>> getConstraintWidgetFactory(Setting<T> setting) {
        SettingConstraint<?> constraint = setting.getConstraint();
        if (constraint != null) {
            for (Class<? extends ConstraintWidgetFactory> widgetType
                    : environment.getSubtypesOf(ConstraintWidgetFactory.class)) {
                Type constraintType =
                        ReflectionUtil.getTypeParameterForSuper(widgetType, ConstraintWidgetFactory.class, 1);

                if (constraint.getClass().equals(ReflectionUtil.getRawType(constraintType))) {

                    ConstraintWidgetFactory<T, ?> factory =
                            InjectionHelper.createWithConstructorInjection(widgetType, context);
                    InjectionHelper.inject(factory, In.class, ImmutableMap.of(AssetManager.class, assetManager));

                    return Optional.of(factory);

                }
            }
        }
        return Optional.empty();
    }
}
