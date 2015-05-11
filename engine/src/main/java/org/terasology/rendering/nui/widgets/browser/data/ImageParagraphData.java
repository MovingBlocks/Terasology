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
package org.terasology.rendering.nui.widgets.browser.data;

import org.terasology.math.Rect2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.widgets.browser.ui.ParagraphRenderable;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.TextRenderStyle;

public class ImageParagraphData implements ParagraphData, ParagraphRenderable {
    private ParagraphRenderStyle paragraphRenderStyle;
    private TextureRegion textureRegion;

    public ImageParagraphData(ParagraphRenderStyle paragraphRenderStyle, TextureRegion textureRegion) {
        this.paragraphRenderStyle = paragraphRenderStyle;
        this.textureRegion = textureRegion;
    }

    @Override
    public ParagraphRenderStyle getParagraphRenderStyle() {
        return paragraphRenderStyle;
    }

    @Override
    public ParagraphRenderable getParagraphContents() {
        return this;
    }

    @Override
    public void render(Canvas canvas, Rect2i region, TextRenderStyle defaultStyle, HorizontalAlign horizontalAlign, HyperlinkRegister hyperlinkRegister) {
        if (region.width() >= textureRegion.getWidth()) {
            int alignOffset = horizontalAlign.getOffset(textureRegion.getWidth(), region.width());
            canvas.drawTexture(textureRegion, Rect2i.createFromMinAndSize(alignOffset + region.minX(), region.minY(), textureRegion.getWidth(), textureRegion.getHeight()));
        } else {
            int width = region.width();
            float ratio = 1f * textureRegion.getHeight() / textureRegion.getWidth();
            int height = Math.round(width * ratio);
            canvas.drawTexture(textureRegion, Rect2i.createFromMinAndSize(region.minX(), region.minY(), width, height));
        }
    }

    @Override
    public int getPreferredHeight(TextRenderStyle defaultStyle, int width) {
        if (width >= textureRegion.getWidth()) {
            return textureRegion.getHeight();
        } else {
            float ratio = 1f * textureRegion.getHeight() / textureRegion.getWidth();
            return Math.round(width * ratio);
        }
    }

    @Override
    public int getMinWidth(TextRenderStyle defaultStyle) {
        return textureRegion.getWidth();
    }
}
