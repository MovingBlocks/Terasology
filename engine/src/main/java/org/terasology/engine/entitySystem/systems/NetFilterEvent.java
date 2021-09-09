// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.systems;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks event handlers condition. when should be registered. {@link RegisterMode}
 * Using with {@link org.terasology.gestalt.entitysystem.event.ReceiveEvent}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NetFilterEvent {

    RegisterMode netFilter();
}
