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
package org.terasology.rendering.assets.shader;

import java.util.Collection;
import java.util.EnumSet;

/**
 */
public enum ShaderProgramFeature {
    FEATURE_REFRACTIVE_PASS
            (0b00000001),
    FEATURE_ALPHA_REJECT
            (0b00000010),
    FEATURE_LIGHT_POINT
            (0b00000100),
    FEATURE_LIGHT_DIRECTIONAL
            (0b00001000),
    FEATURE_USE_MATRIX_STACK
            (0b00100000),
    FEATURE_USE_FORWARD_LIGHTING
            (0b01000000);

    static {
        allBitset = getBitset(EnumSet.allOf(ShaderProgramFeature.class));
    }

    private static int allBitset;
    private int value;

    private ShaderProgramFeature(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static int getBitset(Collection<ShaderProgramFeature> features) {
        int result = 0;
        for (ShaderProgramFeature feature : features) {
            result |= feature.getValue();
        }
        return result;
    }

    public static int getAllBitset() {
        return allBitset;
    }

    public static Iterable<EnumSet<ShaderProgramFeature>> iteratePermutations(EnumSet<ShaderProgramFeature> features) {

        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
