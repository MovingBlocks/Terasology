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

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.utilities.ReflectionUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 */
public class UIStyleFamily {
    private UIStyle baseStyle;
    private Map<Class<? extends UIWidget>, Table<String, String, UIStyle>> elementStyleLookup = Maps.newHashMap();

    private Map<Class<? extends UIWidget>, List<Class<? extends UIWidget>>> cachedInheritanceTree = new WeakHashMap<>();

    public UIStyleFamily(UIStyle baseStyle, Map<Class<? extends UIWidget>, Table<String, String, UIStyle>> elementStyles) {
        this.baseStyle = baseStyle;
        this.elementStyleLookup = elementStyles;
    }

    public UIStyle getBaseStyle() {
        return baseStyle;
    }

    public Iterable<Class<? extends UIWidget>> getWidgets() {
        return elementStyleLookup.keySet();
    }

    public Iterable<String> getPartsFor(Class<? extends UIWidget> widget) {
        Table<String, String, UIStyle> styles = elementStyleLookup.get(widget);
        if (styles == null) {
            return Collections.emptyList();
        }
        return styles.rowKeySet();
    }

    public Iterable<String> getModesFor(Class<? extends UIWidget> widget, String part) {
        Table<String, String, UIStyle> styles = elementStyleLookup.get(widget);
        if (styles == null) {
            return Collections.emptyList();
        }
        return styles.row(part).keySet();
    }

    public UIStyle getElementStyle(Class<? extends UIWidget> element) {
        List<Class<? extends UIWidget>> classes = getInheritanceTree(element);
        UIStyle style = null;
        for (int i = classes.size() - 1; i >= 0 && style == null; i--) {
            Table<String, String, UIStyle> elementStyles = elementStyleLookup.get(classes.get(i));
            if (elementStyles != null) {
                style = elementStyles.get("", "");
            }
        }
        if (style == null) {
            return baseStyle;
        }
        return style;
    }

    public UIStyle getElementStyle(Class<? extends UIWidget> element, String part, String mode) {
        List<Class<? extends UIWidget>> classes = getInheritanceTree(element);
        UIStyle style = null;
        for (int i = classes.size() - 1; i >= 0 && style == null; i--) {
            Table<String, String, UIStyle> elementStyles = elementStyleLookup.get(classes.get(i));
            if (elementStyles != null) {
                style = elementStyles.get(part, mode);
                if (style == null && part.equals(UIWidget.BASE_PART)) {
                    style = elementStyles.get("", mode);
                }
                if (style == null) {
                    style = elementStyles.get(part, "");
                }
            }
        }
        if (style == null) {
            return getElementStyle(element);
        }
        return style;
    }

    private List<Class<? extends UIWidget>> getInheritanceTree(Class<? extends UIWidget> element) {
        List<Class<? extends UIWidget>> classes = cachedInheritanceTree.get(element);
        if (classes == null) {
            classes = ReflectionUtil.getInheritanceTree(element, UIWidget.class);
            cachedInheritanceTree.put(element, classes);
        }
        return classes;
    }
}
