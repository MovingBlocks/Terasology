// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering;

import org.terasology.math.TeraMath;

/**
 */
public final class RenderMath {
    private RenderMath() {
    }

    public static int packColor(float r, float g, float b, float a) {
        int iR = (int) (TeraMath.clamp(r, 0.0f, 1.0f) * 255.0f);
        int iG = (int) (TeraMath.clamp(g, 0.0f, 1.0f) * 255.0f);
        int iB = (int) (TeraMath.clamp(b, 0.0f, 1.0f) * 255.0f);
        int iA = (int) (TeraMath.clamp(a, 0.0f, 1.0f) * 255.0f);

        return iA << 24 | iB << 16 | iG << 8 | iR;
    }
}
