// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.event;

import org.terasology.gestalt.entitysystem.component.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark up methods that can be registered to receive events through the EventSystem
 * <br><br>
 * These methods should have the form
 * <code>public void handlerMethod(EventType event, EntityRef entity)</code>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReceiveEvent {
    /**
     * What components that the entity must have for this method to be invoked
     */
    Class<? extends Component>[] components() default {};

    int priority() default EventPriority.PRIORITY_NORMAL;

    String activity() default "";
}
