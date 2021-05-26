// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.device;

import org.terasology.engine.core.internal.TimeBase;

public final class TimeSystem extends TimeBase {

    public TimeSystem() {
        super(System.nanoTime() / 1000000);
    }

    @Override
    public long getRawTimeInMs() {
        return System.nanoTime() / 1000000;
    }
}
