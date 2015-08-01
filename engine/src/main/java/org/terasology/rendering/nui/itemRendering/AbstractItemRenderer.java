/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.itemRendering;

import org.terasology.math.geom.Rect2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.SubRegion;

/**
 *
 */
public abstract class AbstractItemRenderer<T> implements ItemRenderer<T> {

    @Override
    public void draw(T value, Canvas canvas, Rect2i subregion) {
        try (SubRegion ignored = canvas.subRegion(subregion, false)) {
            draw(value, canvas);
        }
    }

    @Override
    public String getTooltip(T value) {
        return null;
    }
}
