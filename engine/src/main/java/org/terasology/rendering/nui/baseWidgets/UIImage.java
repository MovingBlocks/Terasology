/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.baseWidgets;

import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.skin.UIStyle;
import org.terasology.rendering.nui.UIWidget;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
public class UIImage implements UIWidget {
    private Texture texture;

    private UIStyle style = new UIStyle();
    private Vector2f subregionOffset = new Vector2f(0, 0);
    private Vector2f subregionSize = new Vector2f(1, 1);

    public UIImage() {
    }

    public UIImage(Texture texture) {
        this.texture = texture;
    }

    public UIImage(Texture texture, UIStyle style) {
        this.texture = texture;
        this.style = style;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBackground();
        if (texture != null) {
            canvas.drawTexture(texture, subregionOffset.x, subregionOffset.y, subregionSize.x, subregionSize.y);
        }
    }

    @Override
    public void update(float delta) {
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public UIStyle getStyle() {
        return style;
    }

    public void setStyle(UIStyle style) {
        this.style = style;
    }

    public Vector2f getSubregionOffset() {
        return subregionOffset;
    }

    public void setSubregionOffset(Vector2f subregionOffset) {
        this.subregionOffset.set(subregionOffset);
    }

    public Vector2f getSubregionSize() {
        return subregionSize;
    }

    public void setSubregionSize(Vector2f subregionSize) {
        this.subregionSize.set(subregionSize);
    }
}
