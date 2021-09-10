// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow;

import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.Canvas;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.TextRenderStyle;

public interface FlowRenderable<T extends FlowRenderable<T>> {
    void render(Canvas canvas, Rectanglei bounds, TextRenderStyle defaultRenderStyle);

    int getMinWidth(TextRenderStyle defaultRenderStyle);

    int getWidth(TextRenderStyle defaultRenderStyle);

    int getHeight(TextRenderStyle defaultRenderStyle);

    String getAction();

    SplitResult<T> splitAt(TextRenderStyle defaultRenderStyle, int width);

    class SplitResult<T> {
        public final T before;
        public final T rest;

        public SplitResult(T before, T rest) {
            this.before = before;
            this.rest = rest;
        }
    }
}
