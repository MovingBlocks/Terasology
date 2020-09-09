// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.internal;

import org.lwjgl.Sys;

public final class TimeLwjgl extends TimeBase {

    public TimeLwjgl() {
        super((Sys.getTime() * 1000) / Sys.getTimerResolution());
    }

    @Override
    public long getRawTimeInMs() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
}
