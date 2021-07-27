// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for events that are consumed by the server. If the event is sent on a client, then it will be replicated
 * to the server and actioned there.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServerEvent {
    /**
     * @return Whether the event should be compensated for lag - if true then the location and rotation of all characters
     *         is rewound to simulate the condition on the client before processing the event.
     */
    boolean lagCompensate() default false;
}
