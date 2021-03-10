// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow;

import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.Canvas;
import org.terasology.nui.UITextureRegion;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.TextRenderStyle;

public class ImageFlowRenderable implements FlowRenderable<ImageFlowRenderable> {
    private UITextureRegion textureRegion;
    private int imageWidth;
    private int imageHeight;
    private String hyperlink;

    public ImageFlowRenderable(UITextureRegion textureRegion, Integer width, Integer height, String hyperlink) {
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
    public void render(Canvas canvas, Rectanglei bounds, TextRenderStyle defaultRenderStyle) {
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
