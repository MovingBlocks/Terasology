// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible.ui;

import org.terasology.context.annotation.IndexInherited;
import org.terasology.engine.config.flexible.Setting;
import org.terasology.engine.config.flexible.constraints.SettingConstraint;
import org.terasology.nui.UIWidget;

import java.util.Optional;

/**
 * Creates {@link UIWidget}s by {@link Setting} and used {@link SettingConstraint}
 * <p>
 * You should to implement {@link ConstraintWidgetFactory} if you what use custom type
 * in your {@link org.terasology.engine.config.flexible.AutoConfig}
 * 
 * @param <T> type of setting
 * @param <C> concrete type of {@link SettingConstraint}
 */
@IndexInherited
public abstract class ConstraintWidgetFactory<T, C extends SettingConstraint<T>> {
    private Setting<T> setting;

    protected final T castToT(Object value) {
        return getSetting().getValueType().getRawType().cast(value);
    }

    protected final Setting<T> getSetting() {
        return setting;
    }

    @SuppressWarnings("unchecked")
    protected C getConstraint() {
        return (C) setting.getConstraint();
    }

    public Optional<UIWidget> buildWidgetFor(Setting<T> setting) {
        this.setting = setting;

        return buildWidget();
    }

    protected abstract Optional<UIWidget> buildWidget();
}
