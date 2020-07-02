// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.splash.widgets;

import org.terasology.engine.splash.graphics.Color;
import org.terasology.engine.splash.graphics.Renderer;
import org.terasology.engine.splash.graphics.Texture;

public class BorderedRectangle implements Widget {

    /**
     * onepixel texture.
     */
    private Texture pixel;

    private float x;
    private float y;
    private float width;
    private float height;

    public BorderedRectangle(Texture pixel, int x, int y, int width, int height) {
        this.pixel = pixel;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(Renderer renderer) {
        renderer.begin();
        pixel.bind();
        drawRect(renderer, x, y, width, height, Color.BLACK);
        drawRect(renderer, x + 1, y + 1, width - 2, height - 2, Color.WHITE);
        renderer.end();
    }

    private static void drawRect(Renderer renderer, float x, float y, float width, float height, Color color) {
        renderer.drawTextureRegion(
                x, y,
                x + width, y + height,
                0f, 0f,
                1f, 1f,
                color);
    }
}
