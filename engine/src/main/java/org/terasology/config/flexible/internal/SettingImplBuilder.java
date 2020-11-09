// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.config.flexible.internal;

import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.reflection.TypeInfo;

import java.util.function.Function;

public class SettingImplBuilder<T> implements SettingBuilder<T> {
    private T defaultValue;
    private SettingConstraint<T> constraint;
    private String humanReadableName = "";
    private String description = "";
    private TypeInfo<T> valueType;
    private String systemProperty;
    private Function<String, T> converter;

    @Override
    public SettingBuilder<T> valueType(TypeInfo<T> valueType) {
        this.valueType = valueType;

        return this;
    }

    @Override
    public SettingBuilder<T> defaultValue(T defaultValue) {
        this.defaultValue = defaultValue;

        return this;
    }

    @Override
    public SettingBuilder<T> constraint(SettingConstraint<T> constraint) {
        this.constraint = constraint;

        return this;
    }

    @Override
    public SettingBuilder<T> humanReadableName(String humanReadableName) {
        this.humanReadableName = humanReadableName;

        return this;
    }

    @Override
    public SettingBuilder<T> description(String description) {
        this.description = description;

        return this;

    }

    @Override
    public SettingBuilder<T> systemProperty(String systemProperty, Function<String, T> converter) {
        this.systemProperty = systemProperty;
        this.converter = converter;
        return this;
    }

    @Override
    public Setting<T> build() {
        return new SettingImpl<>(valueType, defaultValue, constraint, humanReadableName, description, systemProperty,
                converter);
    }
}
