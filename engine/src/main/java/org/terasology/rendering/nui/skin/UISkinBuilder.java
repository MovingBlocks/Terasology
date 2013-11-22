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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.UIElement;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.utilities.ReflectionUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class UISkinBuilder {

    private Set<String> families = Sets.newLinkedHashSet();
    private Set<Class<? extends UIElement>> elementClasses = Sets.newLinkedHashSet();
    private SetMultimap<Class<? extends UIElement>, String> modes = HashMultimap.create();

    private Map<String, UIStyleFragment> baseStyles = Maps.newHashMap();
    private Map<String, Table<Class<? extends UIElement>, String, UIStyleFragment>> elementStyles = Maps.newHashMap();

    private UIStyleFragment currentStyle = new UIStyleFragment();
    private String currentFamily = "";
    private Class<? extends UIElement> currentElement;
    private String currentMode = "";

    private void saveStyle() {
        if (currentElement != null) {
            Table<Class<? extends UIElement>, String, UIStyleFragment> elementTable = getElementTable(currentFamily);
            elementTable.put(currentElement, currentMode, currentStyle);
        } else {
            baseStyles.put(currentFamily, currentStyle);
        }
        currentStyle = new UIStyleFragment();
    }

    private Table<Class<? extends UIElement>, String, UIStyleFragment> getElementTable(String family) {
        Table<Class<? extends UIElement>, String, UIStyleFragment> elementTable = elementStyles.get(family);
        if (elementTable == null) {
            elementTable = HashBasedTable.create();
            elementStyles.put(family, elementTable);
        }
        return elementTable;
    }

    public UISkinBuilder setFamily(String family) {
        saveStyle();
        families.add(family);
        currentFamily = family;
        currentElement = null;
        currentMode = "";
        return this;
    }

    public UISkinBuilder setElementClass(Class<? extends UIElement> widget) {
        saveStyle();
        elementClasses.add(widget);
        currentElement = widget;
        currentMode = "";
        return this;

    }

    public UISkinBuilder setElementMode(String mode) {
        if (currentElement == null) {
            throw new IllegalStateException("Element class must be set before element mode");
        }
        saveStyle();
        modes.put(currentElement, mode);
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

        UIStyle defaultStyle = new UIStyle();
        baseStyles.get("").applyTo(defaultStyle);
        Table<Class<? extends UIElement>, String, UIStyle> defaultElementStyles = buildDefaultElementStyles(defaultStyle);
        skinFamilies.put("", new UIStyleFamily(defaultStyle, defaultElementStyles));
        families.remove("");

        for (String family : families) {
            skinFamilies.put(family, buildFamily(family, defaultStyle));
        }
        return new UISkinData(skinFamilies);
    }

    private UIStyleFamily buildFamily(String family, UIStyle defaultStyle) {
        UIStyle baseStyle = new UIStyle(defaultStyle);
        UIStyleFragment fragment = baseStyles.get(family);
        fragment.applyTo(baseStyle);

        Table<Class<? extends UIElement>, String, UIStyle> familyStyles = HashBasedTable.create();
        Table<Class<? extends UIElement>, String, UIStyleFragment> table = getElementTable(family);
        for (Class<? extends UIElement> element : elementClasses) {
            UIStyle elementStyle = new UIStyle(baseStyle);

            UIStyleFragment elementFrag = table.get(element, "");
            UIStyleFragment defaultElementFrag = getDefaultElementStyleFrag(element);
            if (defaultElementFrag != null) {
                defaultElementFrag.applyTo(elementStyle);
            }
            if (elementFrag != null) {
                elementFrag.applyTo(elementStyle);
            }
            if (elementFrag != null || defaultElementFrag != null) {
                familyStyles.put(element, "", elementStyle);
                for (String mode : modes.get(element)) {
                    UIStyleFragment defaultMode = getDefaultElementModeFrag(element, mode);
                    UIStyleFragment elementMode = table.get(element, mode);

                    UIStyle elementModeStyle = new UIStyle(elementStyle);
                    if (defaultMode != null) {
                        defaultMode.applyTo(elementModeStyle);
                    }
                    if (elementMode != null) {
                        elementMode.applyTo(elementModeStyle);
                    }
                    familyStyles.put(element, mode, elementModeStyle);
                }
            }
        }
        return new UIStyleFamily(baseStyle, familyStyles);
    }

    private UIStyleFragment getDefaultElementModeFrag(Class<? extends UIElement> element, String mode) {
        Table<Class<? extends UIElement>, String, UIStyleFragment> elementTable = getElementTable("");
        return elementTable.get(element, mode);
    }

    private UIStyleFragment getDefaultElementStyleFrag(Class<? extends UIElement> element) {
        return getDefaultElementModeFrag(element, "");
    }

    private Table<Class<? extends UIElement>, String, UIStyle> buildDefaultElementStyles(UIStyle defaultStyle) {
        Table<Class<? extends UIElement>, String, UIStyle> results = HashBasedTable.create();
        Table<Class<? extends UIElement>, String, UIStyleFragment> defaultTable = getElementTable("");
        for (Class<? extends UIElement> element : elementClasses) {
            UIStyleFragment fragment = defaultTable.get(element, "");
            if (fragment != null) {
                UIStyle style = new UIStyle(defaultStyle);

                List<Class<? extends UIElement>> inheritanceTree = ReflectionUtil.getInheritanceTree(element, UIElement.class);
                for (Class<? extends UIElement> elementType : inheritanceTree) {
                    UIStyleFragment frag = defaultTable.get(elementType, "");
                    if (frag != null) {
                        frag.applyTo(style);
                    }
                }
                results.put(element, "", style);

                Map<String, UIStyle> modeStyles = Maps.newLinkedHashMap();
                for (Class<? extends UIElement> elementType : inheritanceTree) {
                    for (String state : modes.get(elementType)) {
                        UIStyleFragment stateFrag = defaultTable.get(element, state);
                        if (stateFrag != null) {
                            UIStyle modeStyle = modeStyles.get(state);
                            if (modeStyle == null) {
                                modeStyle = new UIStyle(style);
                                modeStyles.put(state, modeStyle);
                            }
                            stateFrag.applyTo(modeStyle);
                        }

                    }
                }

                for (Map.Entry<String, UIStyle> entry : modeStyles.entrySet()) {
                    results.put(element, entry.getKey(), entry.getValue());
                }
            }
        }
        return results;
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
}
