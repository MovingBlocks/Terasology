// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type or field to be replicated. For events, fields default to replicated so this isn't needed
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Replicate {
    /**
     * @return Under what condition the field should be replicated
     */
    FieldReplicateType value() default FieldReplicateType.SERVER_TO_CLIENT;

    /**
     * @return Whether the field should only be replicated when the entity initially becomes relevant to a client
     */
    boolean initialOnly() default false;
}
