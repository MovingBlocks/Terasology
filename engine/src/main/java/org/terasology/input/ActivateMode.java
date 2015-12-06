/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.input;

/**
 * This enum determines which events a button will send
 *
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

    private boolean activatedOnPress;
    private boolean activatedOnRelease;

    private ActivateMode(boolean activatedOnPress, boolean activatedOnRelease) {
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
