/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.rendering.assets.font;

import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;

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
