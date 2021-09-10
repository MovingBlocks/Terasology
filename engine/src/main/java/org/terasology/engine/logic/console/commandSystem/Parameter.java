// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem;

import java.util.Optional;

/**
 * Classes extending this interface occur in command method parameter definition
 */
@FunctionalInterface
interface Parameter {
    Optional<? extends Class<?>> getProvidedType();
}
