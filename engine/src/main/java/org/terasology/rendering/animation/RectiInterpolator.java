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

import org.terasology.math.geom.Rect2i;

public abstract class RectiInterpolator implements Frame.FrameComponentInterface {
    @Override
    public Object computeInterpolation(float v, Object theFrom, Object theTo) {
        Rect2i f = (Rect2i) theFrom;
        Rect2i t = (Rect2i) theTo;
        return Rect2i.createFromMinAndMax(
            (int) (v * (t.minX() - f.minX()) + f.minX()),
            (int) (v * (t.minY() - f.minY()) + f.minY()),
            (int) (v * (t.maxX() - f.maxX()) + f.maxX()),
            (int) (v * (t.maxY() - f.maxY()) + f.maxY())
        );
    }
}
