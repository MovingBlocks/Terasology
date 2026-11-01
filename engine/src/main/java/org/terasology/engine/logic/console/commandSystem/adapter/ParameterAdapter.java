// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem.adapter;

import org.terasology.engine.logic.console.commandSystem.AbstractCommand;
import org.terasology.context.annotation.API;

/**
 * Used for providing parameters to {@code execute} and {@code suggest} methods of {@link AbstractCommand}
 *
 */
@API
public interface ParameterAdapter<T> {
    T parse(String raw);
    String convertToString(T value);
}
