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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.terasology.math.Border;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.utilities.ReflectionUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 */
public class UISkinBuilder {

    private UISkin baseSkin;
    private Set<String> families = Sets.newLinkedHashSet();
    private Set<StyleKey> baseStyleKeys = Sets.newLinkedHashSet();

    private Map<String, UIStyleFragment> baseStyles = Maps.newHashMap();
    private Table<String, StyleKey, UIStyleFragment> elementStyles = HashBasedTable.create();

    private UIStyleFragment currentStyle = new UIStyleFragment();
    private String currentFamily = "";
    private Class<? extends UIWidget> currentElement;
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

    public UISkinBuilder setBaseSkin(UISkin skin) {
        this.baseSkin = skin;
        return this;
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

    public UISkinBuilder setElementClass(Class<? extends UIWidget> widget) {
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
        currentStyle.setBackground(background);
        return this;
    }

    public UISkinBuilder setBackgroundBorder(Border border) {
        currentStyle.setBackgroundBorder(border);
        return this;
    }

    public UISkinBuilder setBackgroundMode(ScaleMode mode) {
        currentStyle.setBackgroundScaleMode(mode);
        return this;
    }

    public UISkinBuilder setFixedWidth(int width) {
        currentStyle.setFixedWidth(width);
        return this;
    }

    public UISkinBuilder setFixedHeight(int height) {
        currentStyle.setFixedHeight(height);
        return this;
    }

    public UISkinBuilder setHorizontalAlignment(HorizontalAlign align) {
        currentStyle.setAlignmentH(align);
        return this;
    }

    public UISkinBuilder setVerticalAlignment(VerticalAlign align) {
        currentStyle.setAlignmentV(align);
        return this;
    }

    public UISkinBuilder setMargin(Border margin) {
        currentStyle.setMargin(margin);
        return this;
    }

    public UISkinBuilder setTextureScaleMode(ScaleMode scaleMode) {
        currentStyle.setTextureScaleMode(scaleMode);
        return this;
    }

    public UISkinBuilder setFont(Font font) {
        currentStyle.setFont(font);
        return this;
    }

    public UISkinBuilder setTextColor(Color color) {
        currentStyle.setTextColor(color);
        return this;
    }

    public UISkinBuilder setTextShadowColor(Color color) {
        currentStyle.setTextShadowColor(color);
        return this;
    }

    public UISkinBuilder setTextShadowed(boolean shadowed) {
        currentStyle.setTextShadowed(shadowed);
        return this;
    }

    public UISkinBuilder setTextHorizontalAlignment(HorizontalAlign hAlign) {
        currentStyle.setTextAlignmentH(hAlign);
        return this;
    }

    public UISkinBuilder setTextVerticalAlignment(VerticalAlign vAlign) {
        currentStyle.setTextAlignmentV(vAlign);
        return this;
    }

    public UISkinBuilder setTextUnderlined(boolean underlined) {
        currentStyle.setTextUnderlined(underlined);
        return this;
    }

    public UISkinBuilder setStyleFragment(UIStyleFragment fragment) {
        currentStyle = fragment;
        return this;
    }

    public UISkinData build() {
        saveStyle();
        Map<String, UIStyleFamily> skinFamilies = Maps.newHashMap();

        if (baseSkin != null) {
            UIStyle rootStyle = new UIStyle(baseSkin.getDefaultStyle());
            baseStyles.get("").applyTo(rootStyle);
            skinFamilies.put("", buildFamily("", baseSkin));
            for (String family : families) {
                skinFamilies.put(family, buildFamily(family, baseSkin));
            }
            for (String family : baseSkin.getFamilies()) {
                if (!skinFamilies.containsKey(family)) {
                    skinFamilies.put(family, baseSkin.getFamily(family));
                }
            }
            return new UISkinData(skinFamilies);
        } else {
            UIStyle rootStyle = new UIStyle();
            baseStyles.get("").applyTo(rootStyle);
            skinFamilies.put("", buildFamily("", rootStyle));
            for (String family : families) {
                skinFamilies.put(family, buildFamily(family, rootStyle));
            }
            return new UISkinData(skinFamilies);
        }
    }

    private UIStyleFamily buildFamily(String family, UISkin skin) {
        UIStyleFamily baseFamily = skin.getFamily(family);
        UIStyle baseStyle = new UIStyle(skin.getDefaultStyleFor(family));
        if (!family.isEmpty()) {
            UIStyleFragment fragment = baseStyles.get(family);
            fragment.applyTo(baseStyle);
        }

        Set<StyleKey> inheritedStyleKey = Sets.newLinkedHashSet();
        for (Class<? extends UIWidget> widget : baseFamily.getWidgets()) {
            inheritedStyleKey.add(new StyleKey(widget, "", ""));
            for (String part : baseFamily.getPartsFor(widget)) {
                inheritedStyleKey.add(new StyleKey(widget, part, ""));
                for (String mode : baseFamily.getModesFor(widget, part)) {
                    inheritedStyleKey.add(new StyleKey(widget, part, mode));
                }
            }
        }

        Map<Class<? extends UIWidget>, Table<String, String, UIStyle>> familyStyles = Maps.newHashMap();
        Map<StyleKey, UIStyleFragment> styleLookup = elementStyles.row(family);
        Map<StyleKey, UIStyleFragment> baseStyleLookup = (family.isEmpty()) ? Maps.<StyleKey, UIStyleFragment>newHashMap() : elementStyles.row("");
        for (StyleKey styleKey : Sets.union(Sets.union(styleLookup.keySet(), baseStyleKeys), inheritedStyleKey)) {
            UIStyle elementStyle = new UIStyle(baseSkin.getStyleFor(family, styleKey.element, styleKey.part, styleKey.mode));
            baseStyles.get("").applyTo(elementStyle);
            baseStyles.get(family).applyTo(elementStyle);
            List<Class<? extends UIWidget>> inheritanceTree = ReflectionUtil.getInheritanceTree(styleKey.element, UIWidget.class);
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

    private UIStyleFamily buildFamily(String family, UIStyle defaultStyle) {
        UIStyle baseStyle = new UIStyle(defaultStyle);
        if (!family.isEmpty()) {
            UIStyleFragment fragment = baseStyles.get(family);
            fragment.applyTo(baseStyle);
        }

        Map<Class<? extends UIWidget>, Table<String, String, UIStyle>> familyStyles = Maps.newHashMap();
        Map<StyleKey, UIStyleFragment> styleLookup = elementStyles.row(family);
        Map<StyleKey, UIStyleFragment> baseStyleLookup = (family.isEmpty()) ? Maps.<StyleKey, UIStyleFragment>newHashMap() : elementStyles.row("");
        for (StyleKey styleKey : Sets.union(styleLookup.keySet(), baseStyleKeys)) {
            UIStyle elementStyle = new UIStyle(baseStyle);
            List<Class<? extends UIWidget>> inheritanceTree = ReflectionUtil.getInheritanceTree(styleKey.element, UIWidget.class);
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

    private void applyStylesForInheritanceTree(List<Class<? extends UIWidget>> inheritanceTree, String part, String mode, UIStyle elementStyle,
                                               Map<StyleKey, UIStyleFragment> styleLookup, Map<StyleKey, UIStyleFragment> baseStyleLookup) {
        for (Class<? extends UIWidget> element : inheritanceTree) {
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

    private static final class StyleKey {
        private Class<? extends UIWidget> element;
        private String part;
        private String mode;

        private StyleKey(Class<? extends UIWidget> element, String part, String mode) {
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
