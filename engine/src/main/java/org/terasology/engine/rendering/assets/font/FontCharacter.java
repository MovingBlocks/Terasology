// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.font;

import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.texture.Texture;

/**
 */
public class FontCharacter {
    private float x;
    private float y;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;
    private int xAdvance;
    private Texture page;
    private Material pageMat;

    public FontCharacter(float x, float y, int width, int height, int xOffset, int yOffset, int xAdvance, Texture page, Material pageMat) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xAdvance = xAdvance;
        this.page = page;
        this.pageMat = pageMat;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getTexWidth() {
        return ((float) width) / page.getWidth();
    }

    public float getTexHeight() {
        return ((float) height) / page.getHeight();
    }

    public int getxOffset() {
        return xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public int getxAdvance() {
        return xAdvance;
    }

    public Texture getPage() {
        return page;
    }

    public Material getPageMat() {
        return pageMat;
    }
}
