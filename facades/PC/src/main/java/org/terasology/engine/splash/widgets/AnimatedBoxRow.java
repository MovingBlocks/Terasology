// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.splash.widgets;

import org.terasology.engine.splash.graphics.Renderer;
import org.terasology.engine.splash.graphics.Texture;

import java.awt.Color;

public class AnimatedBoxRow implements Widget {

    private final Texture pixel;
    private double time;
    private int x;
    private int y;
    private int width;
    private int height;

    private int boxHeight = 18;
    private int boxWidth = 10;
    private int horzSpace = 8;
    private int dx = boxWidth + horzSpace;
    private double animSpeed = 0.5;
    private float hue = 0.6f;

    /**
     * In seconds
     */
    private double delayPerBox = 0.1;

    public AnimatedBoxRow(Texture pixel, int x, int y, int width, int height) {
        this.pixel = pixel;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    private static void drawRect(Renderer renderer, float x, float y, float width, float height,
                                 org.terasology.engine.splash.graphics.Color color) {
        renderer.drawTextureRegion(
                x, y,
                x + width, y + height,
                0f, 0f,
                1f, 1f,
                color);
    }

    @Override
    public void update(double dt) {
        time += dt;
    }

    /**
     * @param hue the new hue value. Only the fractional part is respected.
     */
    public void setHue(float hue) {
        this.hue = hue;
    }

    /**
     * @param animSpeed the new animation speed (iterations per second)
     */
    public void setAnimSpeed(double animSpeed) {
        this.animSpeed = animSpeed;
    }

    @Override
    public void render(Renderer renderer) {

        // full width equals n * boxWidth + (n - 1) * horzSpace
        int boxCount = (width - boxWidth) / (boxWidth + horzSpace) + 1;

        // align right
        int left = x + width - boxCount * dx + horzSpace;


        for (int i = 0; i < boxCount; i++) {
            pixel.bind();
            renderer.begin();
            float sat = (float) Math.sin((time - i * delayPerBox) * Math.PI * animSpeed);
            sat = sat * sat;
            float bright = 1f;
            int rgb = Color.HSBtoRGB(hue, sat, bright);
            Color animColor = new Color(rgb);
            int sizeDiff = (int) (Math.abs(1.0 - 2 * sat) * 1.1);
            int boxX = left + i * dx - sizeDiff / 2;
            int boxY = y + (height - boxHeight - sizeDiff) / 2;

            drawRect(renderer, boxX, boxY, boxWidth + sizeDiff, boxHeight + sizeDiff,
                    org.terasology.engine.splash.graphics.Color.BLACK);

            drawRect(renderer, boxX + 1, boxY + 1, boxWidth + sizeDiff - 2, boxHeight + sizeDiff - 2,
                    new org.terasology.engine.splash.graphics.Color(animColor.getRed(), animColor.getGreen(),
                            animColor.getBlue()));
            renderer.end();
        }

    }

}
