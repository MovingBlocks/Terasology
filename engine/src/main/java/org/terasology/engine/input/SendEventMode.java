// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

/**
 */
public enum SendEventMode {
    /**
     * Send an event every update/frame with the current axis value
     */
    ALWAYS {
        @Override
        public boolean shouldSendEvent(float oldValue, float newValue) {
            return true;
        }
    },
    /**
     * Sends an event every frame in which the current axis value is not zero
     */
    WHEN_NON_ZERO {
        @Override
        public boolean shouldSendEvent(float oldValue, float newValue) {
            return newValue != 0;
        }
    },
    /**
     * Only sends an event when the value of the axis changes
     */
    WHEN_CHANGED {
        @Override
        public boolean shouldSendEvent(float oldValue, float newValue) {
            return oldValue != newValue;
        }
    };

    public abstract boolean shouldSendEvent(float oldValue, float newValue);
}
