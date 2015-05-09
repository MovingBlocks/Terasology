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
package org.terasology.rendering.nui.widgets.browser.ui;

import org.terasology.rendering.nui.widgets.browser.ui.style.TextRenderStyle;
import org.terasology.math.Rect2i;
import org.terasology.rendering.nui.Canvas;

public interface ParagraphRenderable {
    void render(Canvas canvas, Rect2i region, TextRenderStyle defaultStyle, HyperlinkRegister hyperlinkRegister);

    int getPreferredHeight(Canvas canvas, TextRenderStyle defaultStyle, int width);

    int getMinWidth(Canvas canvas, TextRenderStyle defaultStyle);

    public interface HyperlinkRegister {
        void registerHyperlink(Rect2i region, String hyperlink);
    }
}
