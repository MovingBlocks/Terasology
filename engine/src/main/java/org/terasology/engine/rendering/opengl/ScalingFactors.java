// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl;

/**
 * TODO: Add javadocs
 */
public enum ScalingFactors {
    FULL_SCALE(1.0f),
    HALF_SCALE(0.5f),
    QUARTER_SCALE(0.25f),
    ONE_8TH_SCALE(0.125f),
    ONE_16TH_SCALE(0.0625f),
    ONE_32TH_SCALE(0.03125f);

    private final float scale;

    ScalingFactors(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }
}
