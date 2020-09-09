// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

/**
 * This enum determines which events a button will send
 */
public enum ActivateMode {
    /**
     * The button will only send ButtonState.DOWN events
     */
    PRESS(true, false),
    /**
     * The button will only send ButtonState.UP events
     */
    RELEASE(false, true),
    /**
     * The button will send all events
     */
    BOTH(true, true);

    private final boolean activatedOnPress;
    private final boolean activatedOnRelease;

    ActivateMode(boolean activatedOnPress, boolean activatedOnRelease) {
        this.activatedOnPress = activatedOnPress;
        this.activatedOnRelease = activatedOnRelease;
    }

    public boolean isActivatedOnPress() {
        return activatedOnPress;
    }

    public boolean isActivatedOnRelease() {
        return activatedOnRelease;
    }
}
