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
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.widgets.browser.ui.style.TextRenderStyle;

public class ImageFlowRenderable implements FlowRenderable<ImageFlowRenderable> {
    private TextureRegion textureRegion;
    private int imageWidth;
    private int imageHeight;
    private String hyperlink;

    public ImageFlowRenderable(TextureRegion textureRegion, Integer width, Integer height, String hyperlink) {
        this.textureRegion = textureRegion;
        if (width != null && height != null) {
            this.imageWidth = width;
            this.imageHeight = height;
        } else if (width != null) {
            this.imageWidth = width;
            this.imageHeight = Math.round((1f * width / textureRegion.getWidth()) * textureRegion.getHeight());
        } else if (height != null) {
            this.imageHeight = height;
            this.imageWidth = Math.round((1f * height / textureRegion.getHeight()) * textureRegion.getWidth());
        } else {
            this.imageWidth = textureRegion.getWidth();
            this.imageHeight = textureRegion.getHeight();
        }
        this.hyperlink = hyperlink;
    }

    @Override
    public void render(Canvas canvas, Rect2i bounds, TextRenderStyle defaultRenderStyle) {
        canvas.drawTexture(textureRegion, bounds);
    }

    @Override
    public int getMinWidth(TextRenderStyle defaultRenderStyle) {
        return imageWidth;
    }

    @Override
    public int getWidth(TextRenderStyle defaultRenderStyle) {
        return imageWidth;
    }

    @Override
    public int getHeight(TextRenderStyle defaultRenderStyle) {
        return imageHeight;
    }

    @Override
    public String getAction() {
        return hyperlink;
    }

    @Override
    public SplitResult<ImageFlowRenderable> splitAt(TextRenderStyle defaultRenderStyle, int width) {
        // This cannot be split
        return new SplitResult<>(this, null);
    }
}
