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
package org.terasology.rendering;

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
