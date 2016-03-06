/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.utilities.Assets;
import org.terasology.math.Border;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.VerticalAlign;

/**
 */
public class UIStyle {
    private TextureRegion background;
    private Border backgroundBorder = new Border(0, 0, 0, 0);
    private ScaleMode backgroundScaleMode = ScaleMode.STRETCH;

    private Border margin = new Border(0, 0, 0, 0);
    private int fixedWidth;
    private int fixedHeight;
    private int minWidth;
    private int minHeight;
    private int maxWidth = Integer.MAX_VALUE;
    private int maxHeight = Integer.MAX_VALUE;
    private HorizontalAlign alignmentH = HorizontalAlign.CENTER;
    private VerticalAlign alignmentV = VerticalAlign.MIDDLE;

    private ScaleMode textureScaleMode = ScaleMode.STRETCH;

    private Font font = Assets.getFont("engine:default").get();
    private Color textColor = Color.WHITE;
    private Color textShadowColor = Color.BLACK;
    private HorizontalAlign textAlignmentH = HorizontalAlign.CENTER;
    private VerticalAlign textAlignmentV = VerticalAlign.MIDDLE;
    private boolean textShadowed;
    private boolean textUnderlined;

    public UIStyle() {
    }

    public UIStyle(UIStyle other) {
        this.background = other.background;
        this.backgroundBorder = other.backgroundBorder;
        this.backgroundScaleMode = other.backgroundScaleMode;

        this.margin = other.margin;
        this.fixedWidth = other.fixedWidth;
        this.fixedHeight = other.fixedHeight;
        this.minWidth = other.minWidth;
        this.minHeight = other.minHeight;
        this.maxWidth = other.maxWidth;
        this.maxHeight = other.maxHeight;
        this.alignmentH = other.alignmentH;
        this.alignmentV = other.alignmentV;


        this.textureScaleMode = other.textureScaleMode;

        this.font = other.font;
        this.textColor = other.textColor;
        this.textShadowColor = other.textShadowColor;
        this.textShadowed = other.textShadowed;
        this.textAlignmentH = other.textAlignmentH;
        this.textAlignmentV = other.textAlignmentV;
        this.textUnderlined = other.textUnderlined;
    }

    /**
     * The background is a texture that is drawn filling the area of a widget or part, after taking into account size modified but not margin.
     * It is drawn before any contents
     *
     * @return A texture region to render for a background
     */
    public TextureRegion getBackground() {
        return background;
    }

    public void setBackground(TextureRegion background) {
        this.background = background;
    }

    /**
     * The background border is the part of the background texture around the edges that should not scale with the size of the area being drawn.
     * This allows for things like windows to have a take a background and resize it for any area without losing the desired border feel.
     *
     * @return The border of the background texture that should not be resized
     */
    public Border getBackgroundBorder() {
        return backgroundBorder;
    }

    public void setBackgroundBorder(Border backgroundBorder) {
        this.backgroundBorder = backgroundBorder;
    }

    /**
     * The scale mode determines the technique to use when drawing the background to an area that doesn't match the background's size.
     *
     * @return The technique to use when scaling the background texture for different areas.
     */
    public ScaleMode getBackgroundScaleMode() {
        return backgroundScaleMode;
    }

    public void setBackgroundScaleMode(ScaleMode value) {
        this.backgroundScaleMode = value;
    }

    /**
     * The margin is a gap between the outside of an element and the contents. Often this should be the same or a little more than the border.
     * For instance, a window's contents should sit inside the border.
     *
     * @return The margin between the edges of an element and its contents
     */
    public Border getMargin() {
        return margin;
    }

    public void setMargin(Border margin) {
        this.margin = margin;
    }

    /**
     * @return The scale mode for texture contents of an element
     */
    public ScaleMode getTextureScaleMode() {
        return textureScaleMode;
    }

    public void setTextureScaleMode(ScaleMode textureScaleMode) {
        this.textureScaleMode = textureScaleMode;
    }

    /**
     * @return The font to use for any drawn text
     */
    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * @return The color of any drawn text
     */
    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    /**
     * @return The color of any drawn text's shadow.
     */
    public Color getTextShadowColor() {
        return textShadowColor;
    }

    public void setTextShadowColor(Color textShadowColor) {
        this.textShadowColor = textShadowColor;
    }

    /**
     * @return How any drawn text should be horizontally aligned within its region
     */
    public HorizontalAlign getHorizontalTextAlignment() {
        return textAlignmentH;
    }

    public void setHorizontalTextAlignment(HorizontalAlign alignH) {
        this.textAlignmentH = alignH;
    }

    /**
     * @return How any drawn text should be vertically aligned within its region
     */
    public VerticalAlign getVerticalTextAlignment() {
        return textAlignmentV;
    }

    public void setVerticalTextAlignment(VerticalAlign alignV) {
        this.textAlignmentV = alignV;
    }

    /**
     * @return Whether drawn text should have a shadow
     */
    public boolean isTextShadowed() {
        return textShadowed;
    }

    public void setTextShadowed(boolean textShadowed) {
        this.textShadowed = textShadowed;
    }

    /**
     * @return Whether drawn text should be underlined
     */
    public boolean isTextUnderlined() {
        return textUnderlined;
    }

    public void setTextUnderlined(boolean textUnderlined) {
        this.textUnderlined = textUnderlined;
    }

    public void setFixedWidth(int fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    /**
     * The width to draw the element - if non-zero.
     *
     * @return The fixed width to draw the element
     */
    public int getFixedWidth() {
        return fixedWidth;
    }

    public void setFixedHeight(int fixedHeight) {
        this.fixedHeight = fixedHeight;
    }

    /**
     * The height to draw the element - if non-zero.
     *
     * @return The fixed height to draw the element
     */
    public int getFixedHeight() {
        return fixedHeight;
    }

    public void setHorizontalAlignment(HorizontalAlign horizontalAlignment) {
        this.alignmentH = horizontalAlignment;
    }

    /**
     * If the element does not use the full width of the available to it, how should it be aligned
     *
     * @return The horizontal alignment of the element
     */
    public HorizontalAlign getHorizontalAlignment() {
        return alignmentH;
    }

    public void setVerticalAlignment(VerticalAlign verticalAlignment) {
        this.alignmentV = verticalAlignment;
    }

    /**
     * If the element does not use the full height of the available to it, how should it be aligned
     *
     * @return The vertical alignment of the element
     */
    public VerticalAlign getVerticalAlignment() {
        return alignmentV;
    }

    /**
     * @return The minimum width this element can use
     */
    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    /**
     * @return The minimum height this element can use
     */
    public int getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    /**
     * @return The maximum width this element will use
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    /**
     * @return The maximum height this element will use
     */
    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }
}
