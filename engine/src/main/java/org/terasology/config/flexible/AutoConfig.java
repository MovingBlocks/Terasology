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
package org.terasology.config.flexible;

import com.google.common.collect.ImmutableList;
import org.reflections.ReflectionUtils;
import org.terasology.config.flexible.internal.SettingBuilder;
import org.terasology.config.flexible.internal.SettingImplBuilder;
import org.terasology.engine.SimpleUri;
import org.terasology.reflection.TypeInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * Represents a config class that will be automatically initialized and rendered by the engine.
 * All settings must be contained in {@code public static} fields of type {@link Setting}.
 */
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
}
