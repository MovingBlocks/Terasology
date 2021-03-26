// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem;

import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.Optional;

/**
 */
public enum MarkerParameters implements Parameter {
    /**
     * Marks a parameter which is invalid - there is no information on how it should be provided.
     */
    INVALID(Optional.<Class<?>>empty()),

    /**
     * Marks a parameter which should be populated
     */
    SENDER(Optional.of(EntityRef.class));

    private Optional<? extends Class<?>> providedType;

    MarkerParameters(Optional<? extends Class<?>> providedType) {
        this.providedType = providedType;
    }

    @Override
    public Optional<? extends Class<?>> getProvidedType() {
        return providedType;
    }
}
