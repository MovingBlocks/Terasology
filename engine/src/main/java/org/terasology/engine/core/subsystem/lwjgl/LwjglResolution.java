// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.lwjgl.opengl.DisplayMode;
import org.terasology.engine.core.subsystem.Resolution;

public final class LwjglResolution implements Resolution {

    private final DisplayMode displayMode;

    public LwjglResolution(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LwjglResolution that = (LwjglResolution) o;

        return displayMode.equals(that.displayMode);
    }

    @Override
    public int hashCode() {
        return displayMode.hashCode();
    }

    @Override
    public String toString() {
        return displayMode.toString();
    }
}
