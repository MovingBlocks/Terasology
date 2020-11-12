// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.config.flexible.ui;

import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.nui.UIWidget;

import java.util.Optional;

public abstract class ConstraintWidgetFactory<T, C extends SettingConstraint<T>> {
    private Setting<T> setting;

    protected final T castToT(Object value) {
        return getSetting().getValueType().getRawType().cast(value);
    }

    protected final Setting<T> getSetting() {
        return setting;
    }

    @SuppressWarnings({"unchecked"})
    protected C getConstraint() {
        return (C) setting.getConstraint();
    }

    public Optional<UIWidget> buildWidgetFor(Setting<T> setting) {
        this.setting = setting;

        return buildWidget();
    }

    abstract protected Optional<UIWidget> buildWidget();
}
