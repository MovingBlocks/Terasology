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
package org.terasology.rendering.nui;

import org.terasology.asset.Assets;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.texture.Texture;

/**
 * @author Immortius
 */
public class UIStyle {
    private Texture background;
    private Border border = new Border(0, 0, 0, 0);
    private ScaleMode drawMode = ScaleMode.STRETCH;

    private Font font = Assets.getFont("engine:default");
    private Color textColor = Color.WHITE;
    private Color textShadowColor = Color.BLACK;
    private HorizontalAlignment textAlignmentH = HorizontalAlignment.CENTER;
    private VerticalAlignment textAlignmentV = VerticalAlignment.MIDDLE;
    private boolean textShadowed;

    public UIStyle() {
    }

    public UIStyle(UIStyle other) {
        this.background = other.background;
        this.border.set(other.border);
        this.drawMode = other.drawMode;

        this.font = other.font;
        this.textColor = other.textColor;
        this.textShadowColor = other.textShadowColor;
        this.textShadowed = other.textShadowed;
        this.textAlignmentH = other.textAlignmentH;
    }

    public Texture getBackground() {
        return background;
    }

    public void setBackground(Texture background) {
        this.background = background;
    }

    public Border getBorder() {
        return border;
    }

    public void setBorder(Border border) {
        this.border = border;
    }

    public ScaleMode getDrawMode() {
        return drawMode;
    }

    public void setDrawMode(ScaleMode drawMode) {
        this.drawMode = drawMode;
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

    public HorizontalAlignment getTextAlignmentH() {
        return textAlignmentH;
    }

    public void setTextAlignmentH(HorizontalAlignment textAlignmentH) {
        this.textAlignmentH = textAlignmentH;
    }

    public VerticalAlignment getTextAlignmentV() {
        return textAlignmentV;
    }

    public void setTextAlignmentV(VerticalAlignment textAlignmentV) {
        this.textAlignmentV = textAlignmentV;
    }

    public boolean isTextShadowed() {
        return textShadowed;
    }

    public void setTextShadowed(boolean textShadowed) {
        this.textShadowed = textShadowed;
    }
}
