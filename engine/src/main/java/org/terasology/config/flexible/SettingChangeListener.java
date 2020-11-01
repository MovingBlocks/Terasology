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

/**
 * A callback interface which is notified of value changes in a {@link Setting}.
 *
 * @param <T> The type of values stored in the {@link Setting}.
 */
@FunctionalInterface
public interface SettingChangeListener<T> {
    /**
     * Invoked after the value in the given {@link Setting} has been changed.
     */
    void onValueChanged(Setting<T> setting, T oldValue);
}
