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
package org.terasology.engine.config.flexible.internal;

import org.terasology.engine.config.flexible.Setting;
import org.terasology.engine.config.flexible.constraints.SettingConstraint;
import org.terasology.reflection.TypeInfo;

/**
 * A builder for creating a {@link Setting} storing values of type {@link T}.
 *
 * @param <T> The type of values to be stored in the setting.
 */
public interface SettingBuilder<T> {
    /**
     * Sets the type of the value of the {@link Setting}.
     *
     * @param valueType The {@link TypeInfo} describing the value type of the new Setting.
     * @return This builder object.
     */
    SettingBuilder<T> valueType(TypeInfo<T> valueType);

    /**
     * Sets the default value of the {@link Setting}.
     *
     * @param defaultValue The default value of the new Setting.
     * @return This builder object.
     */
    SettingBuilder<T> defaultValue(T defaultValue);

    /**
     * Specifies the {@link SettingConstraint} that will be used by the {@link Setting}.
     *
     * @param constraint The constraint for the setting.
     * @return This builder object.
     */
    SettingBuilder<T> constraint(SettingConstraint<T> constraint);

    /**
     * Specifies the name of the {@link Setting} being created.
     *
     * @param humanReadableName The name of the setting.
     * @return This builder object.
     */
    SettingBuilder<T> humanReadableName(String humanReadableName);

    /**
     * Specifies the description of the {@link Setting} being created.
     *
     * @param description The setting description.
     * @return This builder object.
     */
    SettingBuilder<T> description(String description);

    /**
     * Builds the {@link Setting} with the components that have already been specified and
     * returns it.
     */
    Setting<T> build();
}

