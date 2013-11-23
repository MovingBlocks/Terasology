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

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.terasology.rendering.nui.UIElement;
import org.terasology.utilities.ReflectionUtil;

import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public class UIStyleFamily {
    private UIStyle baseStyle;
    private Map<Class<? extends UIElement>, Table<String, String, UIStyle>> elementStyleLookup = Maps.newHashMap();

    public UIStyleFamily(UIStyle baseStyle, Map<Class<? extends UIElement>, Table<String, String, UIStyle>> elementStyles) {
        this.baseStyle = baseStyle;
        this.elementStyleLookup = elementStyles;
    }

    public UIStyle getBaseStyle() {
        return baseStyle;
    }

    public UIStyle getElementStyle(Class<? extends UIElement> element) {
        List<Class<? extends UIElement>> classes = ReflectionUtil.getInheritanceTree(element, UIElement.class);
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

    public UIStyle getElementStyle(Class<? extends UIElement> element, String part, String mode) {
        List<Class<? extends UIElement>> classes = ReflectionUtil.getInheritanceTree(element, UIElement.class);
        UIStyle style = null;
        for (int i = classes.size() - 1; i >= 0 && style == null; i--) {
            Table<String, String, UIStyle> elementStyles = elementStyleLookup.get(classes.get(i));
            if (elementStyles != null) {
                style = elementStyles.get(part, mode);
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
}
