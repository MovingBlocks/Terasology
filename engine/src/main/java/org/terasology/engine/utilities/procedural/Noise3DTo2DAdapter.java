// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.procedural;

/**
 * @deprecated use {@link Noise} instead
 */
@Deprecated
public class Noise3DTo2DAdapter implements Noise2D {

    private Noise3D noise;
    private float yVal;

    public Noise3DTo2DAdapter(Noise3D noise) {
        this.noise = noise;
    }

    public Noise3DTo2DAdapter(Noise3D noise, float fixedYVal) {
        this(noise);
        yVal = fixedYVal;
    }

    @Override
    public float noise(float x, float y) {
        return noise.noise(x, yVal, y);
    }
}
