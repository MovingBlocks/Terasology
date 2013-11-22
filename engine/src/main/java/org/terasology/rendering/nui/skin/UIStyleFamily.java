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
import com.google.common.collect.Table;
import org.terasology.rendering.nui.UIElement;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.utilities.ReflectionUtil;

import java.util.List;

/**
 * @author Immortius
 */
public class UIStyleFamily {
    private UIStyle baseStyle;
    private Table<Class<? extends UIElement>, String, UIStyle> widgetStyles = HashBasedTable.create();

    public UIStyleFamily(UIStyle baseStyle, Table<Class<? extends UIElement>, String, UIStyle> widgetStyles) {
        this.baseStyle = baseStyle;
        this.widgetStyles = widgetStyles;
    }

    public UIStyle getBaseStyle() {
        return baseStyle;
    }

    public UIStyle getWidgetStyle(Class<? extends UIElement> element) {
        List<Class<? extends UIElement>> classes = ReflectionUtil.getInheritanceTree(element, UIElement.class);
        UIStyle style = null;
        for (int i = classes.size() - 1; i >= 0 && style == null; i--) {
            style = widgetStyles.get(classes.get(i), "");
        }
        if (style == null) {
            return baseStyle;
        }
        return style;
    }

    public UIStyle getElementStyle(Class<? extends UIElement> element, String mode) {
        List<Class<? extends UIElement>> classes = ReflectionUtil.getInheritanceTree(element, UIElement.class);
        UIStyle style = null;
        for (int i = classes.size() - 1; i >= 0 && style == null; i--) {
            style = widgetStyles.get(classes.get(i), mode);
        }
        if (style == null) {
            return getWidgetStyle(element);
        }
        return style;
    }
}
