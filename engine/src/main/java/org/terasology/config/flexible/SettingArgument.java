// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.config.flexible;

import org.terasology.config.flexible.constraints.SettingConstraint;
import org.terasology.config.flexible.internal.SettingBuilder;
import org.terasology.reflection.TypeInfo;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SettingArgument<P, V> {
    private P argument;
    private BiConsumer<SettingBuilder<V>, P> setter;

    private SettingArgument(P argument, BiConsumer<SettingBuilder<V>, P> setter) {
        this.argument = argument;
        this.setter = setter;
    }

    public static <V> SettingArgument<V, V> defaultValue(V defaultValue) {
        // TODO: Add null checks
        return new SettingArgument<>(defaultValue, SettingBuilder::defaultValue);
    }

    public static <V> SettingArgument<TypeInfo<V>, V> type(Class<V> valueClass) {
        return type(TypeInfo.of(valueClass));
    }

    public static <V> SettingArgument<TypeInfo<V>, V> type(TypeInfo<V> valueType) {
        return new SettingArgument<>(valueType, SettingBuilder::valueType);
    }

    public static <V> SettingArgument<SettingConstraint<V>, V> constraint(SettingConstraint<V> constraint) {
        return new SettingArgument<>(constraint, SettingBuilder::constraint);
    }

    public static <V> SettingArgument<String, V> name(String humanReadableName) {
        return new SettingArgument<>(humanReadableName, SettingBuilder::humanReadableName);
    }

    public static <V> SettingArgument<String, V> description(String description) {
        return new SettingArgument<>(description, SettingBuilder::description);
    }

    public static <V> SettingArgument<String, V> systemProperty(String systemProperty,
                                                                Function<String, V> systemPropertyConverter) {
        return new SettingArgument<>(systemProperty, (builder, string) -> builder.systemProperty(string,
                systemPropertyConverter));
    }


    void setInBuilder(SettingBuilder<V> builder) {
        setter.accept(builder, argument);
    }
}
