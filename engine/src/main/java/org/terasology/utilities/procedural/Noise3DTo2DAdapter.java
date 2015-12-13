/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.utilities.procedural;

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
