// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.console;

import org.terasology.engine.entitySystem.event.Event;

@FunctionalInterface
public interface MessageEvent extends Event {

    /**
     * @return The final message, combining all message elements
     */
    Message getFormattedMessage();

}
