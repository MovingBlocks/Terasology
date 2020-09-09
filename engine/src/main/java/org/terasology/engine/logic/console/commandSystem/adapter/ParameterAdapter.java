// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem.adapter;

import org.terasology.gestalt.module.sandbox.API;

/**
 * Used for providing parameters to {@code execute} and {@code suggest} methods of {@link
 * org.terasology.logic.console.commandSystem.AbstractCommand}
 */
@API
public interface ParameterAdapter<T> {
    T parse(String raw);

    String convertToString(T value);
}
