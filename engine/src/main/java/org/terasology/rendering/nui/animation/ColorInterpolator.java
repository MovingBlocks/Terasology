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
package org.terasology.rendering.nui.animation;

import org.terasology.rendering.nui.Color;

public abstract class ColorInterpolator implements Frame.FrameComponentInterface {
    public void computeInterpolation(float v, Object theFrom, Object theTo) {
        Color f = (Color) theFrom;
        Color t = (Color) theTo;
        setValue(new Color(
            clamp(v * (t.r() - f.r()) + f.r()),
            clamp(v * (t.g() - f.g()) + f.g()),
            clamp(v * (t.b() - f.b()) + f.b()),
            clamp(v * (t.a() - f.a()) + f.a())
        ));
    }

    private static int clamp(float c) {
        if (c < 0) {
            c = 0;
        } else if (c > 255) {
            c = 255;
        }

        return (int) c;
    }
}
