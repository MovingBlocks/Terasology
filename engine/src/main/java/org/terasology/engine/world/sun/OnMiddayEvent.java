// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.sun;

import org.terasology.engine.world.time.TimeEventBase;

/**
 * Sent to a world in the middle of daytime
 *
 */
public class OnMiddayEvent extends TimeEventBase {

    public OnMiddayEvent(long worldTimeMS) {
        super(worldTimeMS);
    }
}
