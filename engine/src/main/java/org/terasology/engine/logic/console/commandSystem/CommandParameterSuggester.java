// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.context.annotation.API;

import java.util.Set;

/**
 * A class used for suggesting command parameter values
 *
 * Constructor arguments will be filled from the {@link org.terasology.engine.context.Context} via dependency injection.
 */
@API
@FunctionalInterface
public interface CommandParameterSuggester<T> {
    /**
     * @param resolvedParameters Currently entered values of the types declared in the command method
     * @return A collection of suggested matches.
     */
    Set<T> suggest(EntityRef sender, Object... resolvedParameters);
}
