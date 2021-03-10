// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.sun;

import org.terasology.engine.world.time.TimeEventBase;

/**
 * Sent to a world at the middle of the night
 *
 */
public class OnMidnightEvent extends TimeEventBase {

    public OnMidnightEvent(long worldTimeMS) {
        super(worldTimeMS);
    }
}
