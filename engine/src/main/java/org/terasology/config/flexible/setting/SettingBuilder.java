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
package org.terasology.config.flexible.setting;

import org.terasology.config.flexible.setting.constraints.SettingConstraint;
import org.terasology.engine.SimpleUri;

public class SettingBuilder {
    public static <T> Id<T> ofType(Class<T> clazz) {
        return new Builder<>(clazz);
    }

    private static class Builder<T> implements Id<T>, DefaultValue<T>, Build<T> {
        private SimpleUri id;
        private T defaultValue;
        private Class<T> valueClass;

        private SettingConstraint<T> constraint = null;

        private String humanReadableName = "";
        private String description = "";

        private Builder(Class<T> valueClass) {
            this.valueClass = valueClass;
        }

        @Override
        public DefaultValue<T> id(SimpleUri id) {
            this.id = id;

            return this;
        }

        @Override
        public Build<T> defaultValue(T defaultValue) {
            this.defaultValue = defaultValue;

            return this;
        }

        @Override
        public Build<T> constraint(SettingConstraint<T> constraint) {
            this.constraint = constraint;

            return this;
        }

        @Override
        public Build<T> humanReadableName(String humanReadableName) {
            this.humanReadableName = humanReadableName;

            return this;
        }

        @Override
        public Build<T> description(String description) {
            this.description = description;

            return this;
        }

        @Override
        public Setting<T> build() {
            return new SettingImpl<>(
                    this.id,
                    this.defaultValue,
                    this.constraint,
                    this.humanReadableName,
                    this.description
            );
        }
    }

    public interface Id<T> {
        DefaultValue<T> id(SimpleUri id);
    }

    public interface DefaultValue<T> {
        Build<T> defaultValue(T defaultValue);
    }

    public interface Build<T> {
        Build<T> constraint(SettingConstraint<T> constraint);

        Build<T> humanReadableName(String humanReadableName);

        Build<T> description(String description);

        Setting<T> build();
    }
}

