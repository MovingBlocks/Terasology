/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layouts;

import com.google.api.client.util.Maps;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.UIWidget;

import java.util.Iterator;
import java.util.Map;

public class CardLayout extends CoreLayout<LayoutHint> {
    @LayoutConfig
    private String defaultCard;

    private Map<String, UIWidget> widgets = Maps.newHashMap();

    public CardLayout() {

    }

    public CardLayout(String id) {
        super(id);
    }

    public void addWidget(UIWidget widget) {
        String id = widget.getId();
        if (id == null) {
            throw new IllegalArgumentException("CardLayout requires for each widget to be added to it to have an id");
        }
        widgets.put(id, widget);
    }

    @Override
    public void removeWidget(UIWidget widget) {
        String id = widget.getId();
        if (id != null) {
            widgets.remove(id);
        }
    }

    public void setDisplayedCard(String id) {
        defaultCard = id;
    }

    @Override
    public void addWidget(UIWidget element, LayoutHint hint) {
        addWidget(element);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (defaultCard != null) {
            UIWidget widget = widgets.get(defaultCard);
            if (widget != null) {
                canvas.drawWidget(widget);
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        int maxX = 0;
        int maxY = 0;
        for (UIWidget uiWidget : widgets.values()) {
            Vector2i preferredContentSize = uiWidget.getPreferredContentSize(canvas, sizeHint);
            maxX = Math.max(maxX, preferredContentSize.x);
            maxY = Math.max(maxY, preferredContentSize.y);
        }

        return new Vector2i(maxX, maxY);
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        int maxX = 0;
        int maxY = 0;
        for (UIWidget uiWidget : widgets.values()) {
            Vector2i maxContentSize = uiWidget.getMaxContentSize(canvas);
            maxX = Math.max(maxX, maxContentSize.x);
            maxY = Math.max(maxY, maxContentSize.y);
        }

        return new Vector2i(maxX, maxY);
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return widgets.values().iterator();
    }
}
