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
import org.terasology.rendering.nui.UIWidget;

/**
 * @author Immortius
 */
public class UIStyleFamily {
    private UIStyle baseStyle;
    private Table<Class<? extends UIWidget>, String, UIStyle> widgetStyles = HashBasedTable.create();

    public UIStyleFamily(UIStyle baseStyle, Table<Class<? extends UIWidget>, String, UIStyle> widgetStyles) {
        this.baseStyle = baseStyle;
        this.widgetStyles = widgetStyles;
    }

    public UIStyle getBaseStyle() {
        return baseStyle;
    }

    public UIStyle getWidgetStyle(Class<? extends UIWidget> widget) {
        UIStyle style = widgetStyles.get(widget, "");
        if (style == null) {
            return baseStyle;
        }
        return style;
    }

    public UIStyle getWidgetStyle(Class<? extends UIWidget> widget, String mode) {
        UIStyle style = widgetStyles.get(widget, mode);
        if (style == null) {
            return getWidgetStyle(widget);
        }
        return style;
    }
}
