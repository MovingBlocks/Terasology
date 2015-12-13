/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.console.commandSystem;

import org.terasology.entitySystem.entity.EntityRef;

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

    private MarkerParameters(Optional<? extends Class<?>> providedType) {
        this.providedType = providedType;
    }

    @Override
    public Optional<? extends Class<?>> getProvidedType() {
        return providedType;
    }
}
