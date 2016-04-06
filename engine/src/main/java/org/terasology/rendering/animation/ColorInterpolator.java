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
package org.terasology.rendering.animation;

import org.terasology.rendering.nui.Color;

public abstract class ColorInterpolator implements Frame.FrameComponentInterface {
    @Override
    public Object computeInterpolation(float v, Object theFrom, Object theTo) {
        Color f = (Color) theFrom;
        Color t = (Color) theTo;
        return new Color(
            255,//clamp(v * (t.rf() - f.rf()) + f.rf()),
            0,//clamp(v * (t.gf() - f.gf()) + f.gf()),
            0,//clamp(v * (t.bf() - f.bf()) + f.bf()),
            255//clamp(v * (t.af() - f.af()) + f.af())
        );
    }

    private static float clamp(float c) {
        //return c;
         if (c > 1) {
             c = 1;
        // } else if (c > 255) {
        //     c = 255;
        }

         return c;
    }
}
