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

public class SettingImplBuilder<T> implements SettingBuilder<T> {
    private T defaultValue;
    private SettingConstraint<T> constraint;
    private String humanReadableName = "";
    private String description = "";
    private TypeInfo<T> valueType;

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
    public Setting<T> build() {
        return new SettingImpl<>(valueType, defaultValue, constraint, humanReadableName, description);
    }
}
