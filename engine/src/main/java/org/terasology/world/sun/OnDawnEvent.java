// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.sun;

import org.terasology.engine.world.time.TimeEventBase;

/**
 * Sent to a world on dawn (beginning of a new day)
 *
 */
public class OnDawnEvent extends TimeEventBase {

    public OnDawnEvent(long worldTimeMS) {
        super(worldTimeMS);
    }
}
