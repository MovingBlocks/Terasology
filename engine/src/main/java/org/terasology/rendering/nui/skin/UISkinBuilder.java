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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.UIElement;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.utilities.ReflectionUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Immortius
 */
public class UISkinBuilder {

    private Set<String> families = Sets.newLinkedHashSet();
    private Set<StyleKey> baseStyleKeys = Sets.newLinkedHashSet();

    private Map<String, UIStyleFragment> baseStyles = Maps.newHashMap();
    private Table<String, StyleKey, UIStyleFragment> elementStyles = HashBasedTable.create();

    private UIStyleFragment currentStyle = new UIStyleFragment();
    private String currentFamily = "";
    private Class<? extends UIElement> currentElement;
    private String currentPart = "";
    private String currentMode = "";

    private void saveStyle() {
        if (currentFamily.isEmpty() && currentElement != null) {
            baseStyleKeys.add(new StyleKey(currentElement, currentPart, currentMode));
        }
        if (currentElement != null) {
            elementStyles.put(currentFamily, new StyleKey(currentElement, currentPart, currentMode), currentStyle);
        } else {
            baseStyles.put(currentFamily, currentStyle);
        }
        currentStyle = new UIStyleFragment();
    }

    public UISkinBuilder setFamily(String family) {
        saveStyle();
        families.add(family);
        currentFamily = family;
        currentElement = null;
        currentPart = "";
        currentMode = "";
        return this;
    }

    public UISkinBuilder setElementClass(Class<? extends UIElement> widget) {
        saveStyle();
        currentElement = widget;
        currentMode = "";
        currentPart = "";
        return this;
    }

    public UISkinBuilder setElementPart(String part) {
        if (currentElement == null) {
            throw new IllegalStateException("Element class must be set before element part");
        }
        saveStyle();
        currentPart = part;
        currentMode = "";
        return this;
    }

    public UISkinBuilder setElementMode(String mode) {
        if (currentElement == null) {
            throw new IllegalStateException("Element class must be set before element mode");
        }
        saveStyle();
        currentMode = mode;
        return this;
    }

    public UISkinBuilder setBackground(TextureRegion background) {
        currentStyle.background = background;
        currentStyle.backgroundSet = true;
        return this;
    }

    public UISkinBuilder setBackgroundBorder(Border border) {
        currentStyle.backgroundBorder = border;
        return this;
    }

    public UISkinBuilder setBackgroundMode(ScaleMode mode) {
        currentStyle.backgroundScaleMode = mode;
        return this;
    }

    public UISkinBuilder setBackgroundAutomaticallyDrawn(boolean autoDraw) {
        currentStyle.autoDrawBackground = autoDraw;
        return this;
    }

    public UISkinBuilder setFixedWidth(int width) {
        currentStyle.fixedWidth = width;
        return this;
    }

    public UISkinBuilder setFixedHeight(int height) {
        currentStyle.fixedHeight = height;
        return this;
    }

    public UISkinBuilder setHorizontalAlignment(HorizontalAlign align) {
        currentStyle.alignmentH = align;
        return this;
    }

    public UISkinBuilder setVerticalAlignment(VerticalAlign align) {
        currentStyle.alignmentV = align;
        return this;
    }

    public UISkinBuilder setMargin(Border margin) {
        currentStyle.margin = margin;
        return this;
    }

    public UISkinBuilder setTextureScaleMode(ScaleMode scaleMode) {
        currentStyle.textureScaleMode = scaleMode;
        return this;
    }

    public UISkinBuilder setFont(Font font) {
        currentStyle.font = font;
        return this;
    }

    public UISkinBuilder setTextColor(Color color) {
        currentStyle.textColor = color;
        return this;
    }

    public UISkinBuilder setTextShadowColor(Color color) {
        currentStyle.textShadowColor = color;
        return this;
    }

    public UISkinBuilder setTextShadowed(boolean shadowed) {
        currentStyle.textShadowed = shadowed;
        return this;
    }

    public UISkinBuilder setTextHorizontalAlignment(HorizontalAlign hAlign) {
        currentStyle.textAlignmentH = hAlign;
        return this;
    }

    public UISkinBuilder setTextVerticalAlignment(VerticalAlign vAlign) {
        currentStyle.textAlignmentV = vAlign;
        return this;
    }

    public UISkinData build() {
        saveStyle();
        Map<String, UIStyleFamily> skinFamilies = Maps.newHashMap();

        UIStyle rootStyle = new UIStyle();
        baseStyles.get("").applyTo(rootStyle);
        skinFamilies.put("", buildFamily("", rootStyle));
        for (String family : families) {
            skinFamilies.put(family, buildFamily(family, rootStyle));
        }
        return new UISkinData(skinFamilies);
    }

    private UIStyleFamily buildFamily(String family, UIStyle defaultStyle) {
        UIStyle baseStyle = new UIStyle(defaultStyle);
        if (!family.isEmpty()) {
            UIStyleFragment fragment = baseStyles.get(family);
            fragment.applyTo(baseStyle);
        }

        Map<Class<? extends UIElement>, Table<String, String, UIStyle>> familyStyles = Maps.newHashMap();
        Map<StyleKey, UIStyleFragment> styleLookup = elementStyles.row(family);
        Map<StyleKey, UIStyleFragment> baseStyleLookup = (family.isEmpty()) ? Maps.<StyleKey, UIStyleFragment>newHashMap() : elementStyles.row("");
        for (StyleKey styleKey : Sets.union(styleLookup.keySet(), baseStyleKeys)) {
            UIStyle elementStyle = new UIStyle(baseStyle);
            List<Class<? extends UIElement>> inheritanceTree = ReflectionUtil.getInheritanceTree(styleKey.element, UIElement.class);
            applyStylesForInheritanceTree(inheritanceTree, "", "", elementStyle, styleLookup, baseStyleLookup);

            if (!styleKey.part.isEmpty()) {
                applyStylesForInheritanceTree(inheritanceTree, styleKey.part, "", elementStyle, styleLookup, baseStyleLookup);
            }

            if (!styleKey.mode.isEmpty()) {
                applyStylesForInheritanceTree(inheritanceTree, styleKey.part, styleKey.mode, elementStyle, styleLookup, baseStyleLookup);
            }

            Table<String, String, UIStyle> elementTable = familyStyles.get(styleKey.element);
            if (elementTable == null) {
                elementTable = HashBasedTable.create();
                familyStyles.put(styleKey.element, elementTable);
            }
            elementTable.put(styleKey.part, styleKey.mode, elementStyle);
        }
        return new UIStyleFamily(baseStyle, familyStyles);
    }

    private void applyStylesForInheritanceTree(List<Class<? extends UIElement>> inheritanceTree, String part, String mode, UIStyle elementStyle,
                                               Map<StyleKey, UIStyleFragment> styleLookup, Map<StyleKey, UIStyleFragment> baseStyleLookup) {
        for (Class<? extends UIElement> element : inheritanceTree) {
            StyleKey key = new StyleKey(element, part, mode);
            UIStyleFragment baseElementStyle = baseStyleLookup.get(key);
            if (baseElementStyle != null) {
                baseElementStyle.applyTo(elementStyle);
            }

            UIStyleFragment elemStyle = styleLookup.get(key);
            if (elemStyle != null) {
                elemStyle.applyTo(elementStyle);
            }
        }
    }

    private static class UIStyleFragment {
        private boolean backgroundSet;
        private TextureRegion background;
        private Border backgroundBorder;
        private ScaleMode backgroundScaleMode;

        private Border margin;

        private ScaleMode textureScaleMode;

        private Font font;
        private Color textColor;
        private Color textShadowColor;
        private HorizontalAlign textAlignmentH;
        private VerticalAlign textAlignmentV;
        private Boolean textShadowed;

        private Boolean autoDrawBackground;
        private Integer fixedWidth;
        private Integer fixedHeight;
        private HorizontalAlign alignmentH;
        private VerticalAlign alignmentV;

        public void applyTo(UIStyle style) {
            if (backgroundSet) {
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
            if (autoDrawBackground != null) {
                style.setBackgroundAutomaticallyDrawn(autoDrawBackground);
            }
            if (fixedWidth != null) {
                style.setFixedWidth(fixedWidth);
            }
            if (fixedHeight != null) {
                style.setFixedHeight(fixedHeight);
            }
            if (alignmentH != null) {
                style.setHorizontalAlignment(alignmentH);
            }
            if (alignmentV != null) {
                style.setVerticalAlignment(alignmentV);
            }
        }
    }

    private static final class StyleKey {
        private Class<? extends UIElement> element;
        private String part;
        private String mode;

        private StyleKey(Class<? extends UIElement> element, String part, String mode) {
            this.element = element;
            this.part = part;
            this.mode = mode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof StyleKey) {
                StyleKey other = (StyleKey) obj;
                return Objects.equals(other.element, element) && Objects.equals(other.part, part) && Objects.equals(other.mode, mode);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(element, part, mode);
        }

        @Override
        public String toString() {
            return element.getSimpleName() + ":" + part + ":" + mode;
        }
    }
}
