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
package org.terasology.rendering.nui.skin;

import org.terasology.asset.Assets;
import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.VerticalAlign;

/**
 * @author Immortius
 */
public class UIStyle {
    private TextureRegion background;
    private Border backgroundBorder = new Border(0, 0, 0, 0);
    private ScaleMode backgroundScaleMode = ScaleMode.STRETCH;

    private Border margin = new Border(0, 0, 0, 0);

    private ScaleMode textureScaleMode = ScaleMode.STRETCH;

    private Font font = Assets.getFont("engine:default");
    private Color textColor = Color.WHITE;
    private Color textShadowColor = Color.BLACK;
    private HorizontalAlign textAlignmentH = HorizontalAlign.CENTER;
    private VerticalAlign textAlignmentV = VerticalAlign.MIDDLE;
    private boolean textShadowed;

    public UIStyle() {
    }

    public UIStyle(UIStyle other) {
        this.background = other.background;
        this.backgroundBorder = other.backgroundBorder;
        this.backgroundScaleMode = other.backgroundScaleMode;

        this.margin = other.margin;

        this.textureScaleMode = other.textureScaleMode;

        this.font = other.font;
        this.textColor = other.textColor;
        this.textShadowColor = other.textShadowColor;
        this.textShadowed = other.textShadowed;
        this.textAlignmentH = other.textAlignmentH;
    }

    public TextureRegion getBackground() {
        return background;
    }

    public void setBackground(TextureRegion background) {
        this.background = background;
    }

    public Border getBackgroundBorder() {
        return backgroundBorder;
    }

    public void setBackgroundBorder(Border backgroundBorder) {
        this.backgroundBorder = backgroundBorder;
    }

    public ScaleMode getBackgroundScaleMode() {
        return backgroundScaleMode;
    }

    public void setBackgroundScaleMode(ScaleMode value) {
        this.backgroundScaleMode = value;
    }

    public Border getMargin() {
        return margin;
    }

    public void setMargin(Border margin) {
        this.margin = margin;
    }

    public ScaleMode getTextureScaleMode() {
        return textureScaleMode;
    }

    public void setTextureScaleMode(ScaleMode textureScaleMode) {
        this.textureScaleMode = textureScaleMode;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getTextShadowColor() {
        return textShadowColor;
    }

    public void setTextShadowColor(Color textShadowColor) {
        this.textShadowColor = textShadowColor;
    }

    public HorizontalAlign getTextAlignmentH() {
        return textAlignmentH;
    }

    public void setTextAlignmentH(HorizontalAlign textAlignmentH) {
        this.textAlignmentH = textAlignmentH;
    }

    public VerticalAlign getTextAlignmentV() {
        return textAlignmentV;
    }

    public void setTextAlignmentV(VerticalAlign textAlignmentV) {
        this.textAlignmentV = textAlignmentV;
    }

    public boolean isTextShadowed() {
        return textShadowed;
    }

    public void setTextShadowed(boolean textShadowed) {
        this.textShadowed = textShadowed;
    }
}
