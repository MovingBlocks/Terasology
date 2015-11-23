/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.nui.widgets.browser.data.basic.flow;

import org.terasology.math.geom.Rect2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.widgets.browser.ui.style.TextRenderStyle;

public interface FlowRenderable<T extends FlowRenderable<T>> {
    void render(Canvas canvas, Rect2i bounds, TextRenderStyle defaultRenderStyle);

    int getMinWidth(TextRenderStyle defaultRenderStyle);

    int getWidth(TextRenderStyle defaultRenderStyle);

    int getHeight(TextRenderStyle defaultRenderStyle);

    String getAction();

    SplitResult<T> splitAt(TextRenderStyle defaultRenderStyle, int width);

    public class SplitResult<T> {
        public final T before;
        public final T rest;

        public SplitResult(T before, T rest) {
            this.before = before;
            this.rest = rest;
        }
    }
}
