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
package org.terasology.math;

/**
 * Enumeration for yaw
 * <br><br>
 * Pitch, yaw and roll enumerations are all very similar, but separated to allow for safer usage in Rotation.
 *
 */
public enum Yaw {
    NONE((byte) 0b00, 0, 0),
    CLOCKWISE_90((byte) 0b01, (float) (0.5f * Math.PI), 1),
    CLOCKWISE_180((byte) 0b10, (float) (Math.PI), 2),
    CLOCKWISE_270((byte) 0b11, (float) (-0.5f * Math.PI), 3);

    private byte index;
    private float radians;
    private int increments;

    private Yaw(byte index, float radians, int increments) {
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
