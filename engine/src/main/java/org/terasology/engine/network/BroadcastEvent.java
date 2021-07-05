// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for events that are broadcast to all clients (in addition to running on the server).
 * These events must originate from the server in order to be broadcast.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BroadcastEvent {
    /**
     * If true and applied to a subclass of NetworkEvent, the event will not be sent to the client owning the instigator
     * entity.
     *
     * @return Whether the event should not be sent to its instigator
     */
    boolean skipInstigator() default false;
}
