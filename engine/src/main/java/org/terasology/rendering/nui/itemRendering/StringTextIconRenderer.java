/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.itemRendering;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.TextLineBuilder;

import java.util.List;

/**
 * This ItemRenderer displays text preceded by an icon texture based on a given string.
 */

public abstract class StringTextIconRenderer<T> extends AbstractItemRenderer<T> {

    private final int marginTop;
    private final int marginBottom;
    private final int marginLeft;
    private final int marginRight;

    protected StringTextIconRenderer() {
        this(5, 5, 5, 10);
    }

    protected StringTextIconRenderer(int marginTop, int marginBottom, int marginLeft, int marginRight) {
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
    }

    @Override
    public void draw(T value, Canvas canvas) {
        // Drawing the icon
        TextureRegion texture = getTexture(value);

        if (texture != null) {
            if (marginTop + texture.getHeight() + marginBottom > canvas.size().y) {
                // Icon does not fit within the canvas - vertically shrinking it
                int iconHeight = canvas.size().y - marginTop - marginBottom;
                canvas.drawTexture(texture, Rect2i.createFromMinAndSize(marginLeft, marginTop, texture.getWidth(), iconHeight));
            } else {
                // Icon fits within the canvas - vertically centering it
                int iconVerticalPosition = (canvas.size().y - texture.getHeight()) / 2;
                canvas.drawTexture(texture, Rect2i.createFromMinAndSize(marginLeft, iconVerticalPosition, texture.getWidth(), texture.getHeight()));
            }
        }

        // Drawing the text, adjusting for icon width
        String text = getString(value);

        int iconWidth;
        if (texture != null) {
            iconWidth = marginLeft + texture.getWidth() + marginRight;
        } else {
            iconWidth = 0;
        }

        Rect2i textRegion = Rect2i.createFromMinAndSize(iconWidth, 0, canvas.getRegion().width() - iconWidth, canvas.getRegion().height());
        canvas.drawText(text, textRegion);
    }

    @Override
    public Vector2i getPreferredSize(T value, Canvas canvas) {
        Font font = canvas.getCurrentStyle().getFont();
        String text = getString(value);

        TextureRegion texture = getTexture(value);
        if (texture == null) {
            List<String> lines = TextLineBuilder.getLines(font, text, canvas.size().x);
            return font.getSize(lines);
        } else {
            int iconWidth = marginLeft + texture.getWidth() + marginRight;
            List<String> lines = TextLineBuilder.getLines(font, text, canvas.size().x - iconWidth);
            return font.getSize(lines).addX(iconWidth);
        }
    }

    public abstract String getString(T value);

    public abstract TextureRegion getTexture(T value);
}
