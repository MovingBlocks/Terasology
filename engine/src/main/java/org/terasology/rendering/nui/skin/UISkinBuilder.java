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
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.VerticalAlign;

import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class UISkinBuilder {

    private Set<String> families = Sets.newLinkedHashSet();
    private Set<Class<? extends UIWidget>> widgetClasses = Sets.newLinkedHashSet();
    private SetMultimap<Class<? extends UIWidget>, String> modes = HashMultimap.create();

    private Map<String, UIStyleFragment> baseStyles = Maps.newHashMap();
    private Map<String, Table<Class<? extends UIWidget>, String, UIStyleFragment>> widgetStyles = Maps.newHashMap();

    private UIStyleFragment currentStyle = new UIStyleFragment();
    private String currentFamily = "";
    private Class<? extends UIWidget> currentWidget;
    private String currentMode = "";

    private void saveStyle() {
        if (currentWidget != null) {
            Table<Class<? extends UIWidget>, String, UIStyleFragment> widgetTable = getWidgetTable(currentFamily);
            widgetTable.put(currentWidget, currentMode, currentStyle);
        } else {
            baseStyles.put(currentFamily, currentStyle);
        }
        currentStyle = new UIStyleFragment();
    }

    private Table<Class<? extends UIWidget>, String, UIStyleFragment> getWidgetTable(String family) {
        Table<Class<? extends UIWidget>, String, UIStyleFragment> widgetTable = widgetStyles.get(family);
        if (widgetTable == null) {
            widgetTable = HashBasedTable.create();
            widgetStyles.put(family, widgetTable);
        }
        return widgetTable;
    }

    public UISkinBuilder setFamily(String family) {
        saveStyle();
        families.add(family);
        currentFamily = family;
        currentWidget = null;
        currentMode = "";
        return this;
    }

    public UISkinBuilder setWidgetClass(Class<? extends UIWidget> widget) {
        saveStyle();
        widgetClasses.add(widget);
        currentWidget = widget;
        currentMode = "";
        return this;

    }

    public UISkinBuilder setWidgetMode(String mode) {
        if (currentWidget == null) {
            throw new IllegalStateException("Widget class must be set before widget mode");
        }
        saveStyle();
        modes.put(currentWidget, mode);
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
        Table<Class<? extends UIWidget>, String, UIStyle> defaultWidgetStyles = buildDefaultWidgetStyles(defaultStyle);
        skinFamilies.put("", new UIStyleFamily(defaultStyle, defaultWidgetStyles));
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

        Table<Class<? extends UIWidget>, String, UIStyle> familyStyles = HashBasedTable.create();
        Table<Class<? extends UIWidget>, String, UIStyleFragment> table = getWidgetTable(family);
        for (Class<? extends UIWidget> widget : widgetClasses) {
            UIStyle widgetStyle = new UIStyle(baseStyle);

            UIStyleFragment widgetFrag = table.get(widget, "");
            UIStyleFragment defaultWidgetFrag = getDefaultWidgetStyleFrag(widget);
            if (defaultWidgetFrag != null) {
                defaultWidgetFrag.applyTo(widgetStyle);
            }
            if (widgetFrag != null) {
                widgetFrag.applyTo(widgetStyle);
            }
            if (widgetFrag != null || defaultWidgetFrag != null) {
                familyStyles.put(widget, "", widgetStyle);
                for (String mode : modes.get(widget)) {
                    UIStyleFragment defaultMode = getDefaultWidgetModeFrag(widget, mode);
                    UIStyleFragment widgetMode = table.get(widget, mode);

                    UIStyle widgetModeStyle = new UIStyle(widgetStyle);
                    if (defaultMode != null) {
                        defaultMode.applyTo(widgetModeStyle);
                    }
                    if (widgetMode != null) {
                        widgetMode.applyTo(widgetModeStyle);
                    }
                    familyStyles.put(widget, mode, widgetModeStyle);
                }
            }
        }
        return new UIStyleFamily(baseStyle, familyStyles);
    }

    private UIStyleFragment getDefaultWidgetModeFrag(Class<? extends UIWidget> widget, String mode) {
        Table<Class<? extends UIWidget>, String, UIStyleFragment> widgetTable = getWidgetTable("");
        return widgetTable.get(widget, mode);
    }

    private UIStyleFragment getDefaultWidgetStyleFrag(Class<? extends UIWidget> widget) {
        return getDefaultWidgetModeFrag(widget, "");
    }

    private Table<Class<? extends UIWidget>, String, UIStyle> buildDefaultWidgetStyles(UIStyle defaultStyle) {
        Table<Class<? extends UIWidget>, String, UIStyle> results = HashBasedTable.create();
        Table<Class<? extends UIWidget>, String, UIStyleFragment> defaultTable = getWidgetTable("");
        for (Class<? extends UIWidget> widget : widgetClasses) {
            UIStyleFragment fragment = defaultTable.get(widget, "");
            if (fragment != null) {
                UIStyle style = new UIStyle(defaultStyle);
                fragment.applyTo(style);
                results.put(widget, "", style);

                for (String state : modes.get(widget)) {
                    UIStyleFragment stateFrag = defaultTable.get(widget, state);
                    if (stateFrag != null) {
                        UIStyle modeStyle = new UIStyle(style);
                        stateFrag.applyTo(modeStyle);
                        results.put(widget, state, modeStyle);
                    }
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
        }
    }
}
