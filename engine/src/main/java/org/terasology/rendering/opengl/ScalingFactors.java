/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.opengl;

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
