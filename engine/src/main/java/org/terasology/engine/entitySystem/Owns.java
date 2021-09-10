// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When used in a Component on a EntityRef, {@literal List<EntityRef>} or {@literal Set<EntityRef> field},
 * denotes that the Entity will assume ownership of the entity or entities contained in that field.
 * <br><br>
 * This means:
 * <ul>
 * <li>The owned entity will be persisted and restored along with its owner.</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Owns {
}
