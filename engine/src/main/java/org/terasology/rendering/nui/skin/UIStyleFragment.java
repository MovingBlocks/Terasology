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

import com.google.gson.annotations.SerializedName;
import org.terasology.math.Border;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.VerticalAlign;

import java.util.Optional;

/**
 */
public class UIStyleFragment {
    // This field is intentionally null, so it can represent no change (null), empty and a background
    private Optional<TextureRegion> background;
    @SerializedName("background-border")
    private Border backgroundBorder;
    @SerializedName("background-scale-mode")
    private ScaleMode backgroundScaleMode;

    private Border margin;
    @SerializedName("fixed-width")
    private Integer fixedWidth;
    @SerializedName("fixed-height")
    private Integer fixedHeight;

    @SerializedName("min-width")
    private Integer minWidth;
    @SerializedName("min-height")
    private Integer minHeight;
    @SerializedName("max-width")
    private Integer maxWidth;
    @SerializedName("max-height")
    private Integer maxHeight;

    @SerializedName("align-horizontal")
    private HorizontalAlign alignmentH;
    @SerializedName("align-vertical")
    private VerticalAlign alignmentV;

    @SerializedName("texture-scale-mode")
    private ScaleMode textureScaleMode;

    private Font font;
    @SerializedName("text-color")
    private Color textColor;
    @SerializedName("text-shadow-color")
    private Color textShadowColor;

    @SerializedName("text-align-horizontal")
    private HorizontalAlign textAlignmentH;
    @SerializedName("text-align-vertical")
    private VerticalAlign textAlignmentV;
    @SerializedName("text-shadowed")
    private Boolean textShadowed;
    @SerializedName("text-underlined")
    private Boolean textUnderlined;


    public void applyTo(UIStyle style) {
        if (background != null) {
            style.setBackground(background.orElse(null));
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
            style.setHorizontalTextAlignment(textAlignmentH);
        }
        if (textAlignmentV != null) {
            style.setVerticalTextAlignment(textAlignmentV);
        }
        if (textShadowed != null) {
            style.setTextShadowed(textShadowed);
        }
        if (textUnderlined != null) {
            style.setTextUnderlined(textUnderlined);
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
        return background.orElse(null);
    }

    public void setBackground(TextureRegion background) {
        this.background = Optional.ofNullable(background);
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

    public Boolean getTextUnderlined() {
        return textUnderlined;
    }

    public void setTextUnderlined(Boolean textUnderlined) {
        this.textUnderlined = textUnderlined;
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

    public Integer getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(Integer minWidth) {
        this.minWidth = minWidth;
    }

    public Integer getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(Integer minHeight) {
        this.minHeight = minHeight;
    }

    public Integer getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
    }

    public Integer getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(Integer maxHeight) {
        this.maxHeight = maxHeight;
    }
}
