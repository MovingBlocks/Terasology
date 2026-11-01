// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible;

import com.google.common.collect.ImmutableList;
import org.reflections.ReflectionUtils;
import org.terasology.context.annotation.IndexInherited;
import org.terasology.engine.config.flexible.internal.SettingBuilder;
import org.terasology.engine.config.flexible.internal.SettingImplBuilder;
import org.terasology.engine.core.SimpleUri;
import org.terasology.reflection.TypeInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a config class that will be automatically initialized and rendered by the engine.
 * All settings must be contained in {@code public static} fields of type {@link Setting}.
 */
@IndexInherited
public abstract class AutoConfig {
    private SimpleUri id;

    static Set<Field> getSettingFieldsIn(Class<? extends AutoConfig> configType) {
        return ReflectionUtils.getFields(
            configType,
            ReflectionUtils.withModifier(Modifier.PUBLIC),
            ReflectionUtils.withModifier(Modifier.FINAL),
            ReflectionUtils.withType(Setting.class)
        );
    }

    public Set<Setting<?>> getSettings() {
        return getSettingFieldsIn(getClass()).stream()
                .map(field -> {
                    try {
                        return (Setting<?>) field.get(this);
                    } catch (IllegalAccessException e) {
                        // Setting field will always be accessible, exception should never be thrown
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
    }

    private static <T> Setting<T> setting(Iterable<SettingArgument<?, T>> arguments) {
        SettingBuilder<T> builder = new SettingImplBuilder<>();

        for (SettingArgument<?, T> argument : arguments) {
            argument.setInBuilder(builder);
        }

        return builder.build();
    }

    @SafeVarargs
    protected static <T> Setting<T> setting(SettingArgument<TypeInfo<T>, T> valueType,
                                            SettingArgument<T, T> defaultValue,
                                            SettingArgument<?, T>... arguments) {
        return setting(
            ImmutableList.<SettingArgument<?, T>>builder()
                .add(valueType)
                .add(defaultValue)
                .add(arguments)
                .build()
        );
    }

    public SimpleUri getId() {
        return id;
    }

    void setId(SimpleUri id) {
        this.id = id;
    }

    public abstract String getName();

}
