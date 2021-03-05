// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.sun;

import org.terasology.engine.world.time.TimeEventBase;

/**
 * Sent to the world on dusk (half way through the day, when the sun sets)
 *
 */
public class OnDuskEvent extends TimeEventBase {

    public OnDuskEvent(long worldTimeMS) {
        super(worldTimeMS);
    }
}
