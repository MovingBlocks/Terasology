// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.event;

import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This {@link Event} is fired when the chunk provider purges
 * all chunks. This can be triggered using the "purgeWorld" world command. 
 */
public class PurgeWorldEvent implements Event {
    // empty
}
