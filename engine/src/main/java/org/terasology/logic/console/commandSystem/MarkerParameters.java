package org.terasology.logic.console.commandSystem;

import com.google.common.base.Optional;
import org.terasology.entitySystem.entity.EntityRef;

/**
 * @author Immortius, Limeth
 */
public enum MarkerParameters implements Parameter {
    /**
     * Marks a parameter which is invalid - there is no information on how it should be provided.
     */
    INVALID(Optional.<Class<?>>absent()),

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
