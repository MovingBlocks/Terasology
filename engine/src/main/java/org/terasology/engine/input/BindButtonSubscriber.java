// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * Interface for subscribing to bind button events
 *
 */
public interface BindButtonSubscriber {

    /**
     * Called when the bind is activated
     *
     * @param delta  The time passing this frame
     * @param target The current camera target
     * @return True if the bind event was consumed
     */
    boolean onPress(float delta, EntityRef target);

    /**
     * Called when the bind repeats
     *
     * @param delta  The time this frame (not per repeat)
     * @param target The current camera target
     * @return True if the bind event was consumed
     */
    boolean onRepeat(float delta, EntityRef target);

    /**
     * Called when the bind is deactivated
     *
     * @param delta  The time passing this frame
     * @param target The current camera target
     * @return True if the bind event was consumed
     */
    boolean onRelease(float delta, EntityRef target);
}
