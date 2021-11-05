// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.shader;

import java.util.Collection;

public enum ShaderProgramFeature {
    FEATURE_REFRACTIVE_PASS(0x01),
    FEATURE_ALPHA_REJECT(0x02),
    FEATURE_LIGHT_POINT(0x04),
    FEATURE_LIGHT_DIRECTIONAL(0x08),
    FEATURE_USE_FORWARD_LIGHTING(0x10);

    private int value;

    ShaderProgramFeature(int value) {
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
}
