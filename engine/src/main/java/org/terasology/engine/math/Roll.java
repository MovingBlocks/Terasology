// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.math;

/**
 * Enumeration for Roll.
 * <br><br>
 * Pitch, yaw and roll enumerations are all very similar, but separated to allow for safer usage in Rotation.
 *
 */
public enum Roll {
    NONE((byte) 0b00, 0, 0),
    CLOCKWISE_90((byte) 0b01, (float) (0.5f * Math.PI), 1),
    CLOCKWISE_180((byte) 0b10, (float) (Math.PI), 2),
    CLOCKWISE_270((byte) 0b11, (float) (-0.5f * Math.PI), 3);

    private final byte index;
    private final float radians;
    private final int increments;

     Roll(byte index, float radians, int increments) {
        this.index = index;
        this.radians = radians;
        this.increments = increments;
    }

    public float getRadians() {
        return radians;
    }

    public int getIncrements() {
        return increments;
    }

    public byte getIndex() {
        return index;
    }
}
