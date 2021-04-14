// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config.flexible.ui;

import org.terasology.engine.config.flexible.Setting;
import org.terasology.engine.config.flexible.constraints.SettingConstraint;
import org.terasology.engine.context.Context;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;

import java.util.Optional;

public class DefaultConstraintWidgetFactory<T> extends ConstraintWidgetFactory<T, SettingConstraint<T>> {
    private final Context context;

    public DefaultConstraintWidgetFactory(Context context) {
        this.context = context;
    }

    @Override
    protected Optional<UIWidget> buildWidget() {
        Setting<T> setting = getSetting();
        Binding<T> binding = new Binding<T>() {
            @Override
            public T get() {
                return setting.get();
            }

            @Override
            public void set(T value) {
                setting.set(value);
            }
        };
        return context.get(TypeWidgetLibrary.class).getWidget(binding, setting.getValueType());
    }
}
