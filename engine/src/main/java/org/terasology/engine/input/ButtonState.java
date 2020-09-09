// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

/**
 *
 */
public enum ButtonState {
    DOWN(true),
    UP(false),
    REPEAT(true);

    private final boolean down;

    ButtonState(boolean down) {
        this.down = down;
    }

    public boolean isDown() {
        return down;
    }
}
