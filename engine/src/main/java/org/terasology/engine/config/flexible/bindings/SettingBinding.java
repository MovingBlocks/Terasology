// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config.flexible.bindings;


import org.terasology.engine.config.flexible.Setting;
import org.terasology.nui.databinding.Binding;

public class SettingBinding<T> implements Binding<T> {
    private final Setting<T> setting;

    public SettingBinding(Setting<T> setting) {
        this.setting = setting;
    }


    @Override
    public T get() {
        return setting.get();
    }

    @Override
    public void set(T value) {
        setting.set(value);
    }
}
