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

import com.google.gson.annotations.SerializedName;
import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.VerticalAlign;

/**
 * @author Immortius
 */
public class UIStyleFragment {
    public TextureRegion background;
    @SerializedName("background-border")
    public Border backgroundBorder;
    @SerializedName("background-scale-mode")
    public ScaleMode backgroundScaleMode;
    @SerializedName("background-auto-draw")
    public Boolean backgroundAutomaticallyDrawn;

    public Border margin;
    @SerializedName("fixed-width")
    public Integer fixedWidth;
    @SerializedName("fixed-height")
    public Integer fixedHeight;

    @SerializedName("min-width")
    public Integer minWidth;
    @SerializedName("min-height")
    public Integer minHeight;
    @SerializedName("max-width")
    public Integer maxWidth;
    @SerializedName("max-height")
    public Integer maxHeight;

    @SerializedName("align-horizontal")
    public HorizontalAlign alignmentH;
    @SerializedName("align-vertical")
    public VerticalAlign alignmentV;

    @SerializedName("texture-scale-mode")
    public ScaleMode textureScaleMode;

    public Font font;
    @SerializedName("text-color")
    public Color textColor;
    @SerializedName("text-shadow-color")
    public Color textShadowColor;

    @SerializedName("text-align-horizontal")
    public HorizontalAlign textAlignmentH;
    @SerializedName("text-align-vertical")
    public VerticalAlign textAlignmentV;
    @SerializedName("text-shadowed")
    public Boolean textShadowed;


    public void applyTo(UIStyle style) {
        if (background != null) {
            style.setBackground(background);
        }
        if (backgroundBorder != null) {
            style.setBackgroundBorder(backgroundBorder);
        }
        if (backgroundScaleMode != null) {
            style.setBackgroundScaleMode(backgroundScaleMode);
        }
        if (margin != null) {
            style.setMargin(margin);
        }
        if (textureScaleMode != null) {
            style.setTextureScaleMode(textureScaleMode);
        }
        if (font != null) {
            style.setFont(font);
        }
        if (textColor != null) {
            style.setTextColor(textColor);
        }
        if (textShadowColor != null) {
            style.setTextShadowColor(textShadowColor);
        }
        if (textAlignmentH != null) {
            style.setTextAlignmentH(textAlignmentH);
        }
        if (textAlignmentV != null) {
            style.setTextAlignmentV(textAlignmentV);
        }
        if (textShadowed != null) {
            style.setTextShadowed(textShadowed);
        }
        if (backgroundAutomaticallyDrawn != null) {
            style.setBackgroundAutomaticallyDrawn(backgroundAutomaticallyDrawn);
        }
        if (fixedWidth != null) {
            style.setFixedWidth(fixedWidth);
        }
        if (fixedHeight != null) {
            style.setFixedHeight(fixedHeight);
        }
        if (minWidth != null) {
            style.setMinWidth(minWidth);
        }
        if (minHeight != null) {
            style.setMinHeight(minHeight);
        }
        if (maxWidth != null) {
            style.setMaxWidth(maxWidth);
        }
        if (maxHeight != null) {
            style.setMaxHeight(maxHeight);
        }
        if (alignmentH != null) {
            style.setHorizontalAlignment(alignmentH);
        }
        if (alignmentV != null) {
            style.setVerticalAlignment(alignmentV);
        }
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

    public void setBackgroundScaleMode(ScaleMode backgroundScaleMode) {
        this.backgroundScaleMode = backgroundScaleMode;
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

    public Boolean getTextShadowed() {
        return textShadowed;
    }

    public void setTextShadowed(Boolean textShadowed) {
        this.textShadowed = textShadowed;
    }

    public Boolean getAutoDrawBackground() {
        return backgroundAutomaticallyDrawn;
    }

    public void setAutoDrawBackground(Boolean autoDrawBackground) {
        this.backgroundAutomaticallyDrawn = autoDrawBackground;
    }

    public Integer getFixedWidth() {
        return fixedWidth;
    }

    public void setFixedWidth(Integer fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    public Integer getFixedHeight() {
        return fixedHeight;
    }

    public void setFixedHeight(Integer fixedHeight) {
        this.fixedHeight = fixedHeight;
    }

    public HorizontalAlign getAlignmentH() {
        return alignmentH;
    }

    public void setAlignmentH(HorizontalAlign alignmentH) {
        this.alignmentH = alignmentH;
    }

    public VerticalAlign getAlignmentV() {
        return alignmentV;
    }

    public void setAlignmentV(VerticalAlign alignmentV) {
        this.alignmentV = alignmentV;
    }
}
