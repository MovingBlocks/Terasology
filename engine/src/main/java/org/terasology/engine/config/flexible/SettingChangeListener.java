// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible;

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
