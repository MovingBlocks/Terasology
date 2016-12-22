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

import com.google.common.collect.Maps;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.UIWidget;

import java.util.Iterator;
import java.util.Map;

/**
 * A layout that allows for a single widget to be displayed among a list of stored widgets
 */
public class CardLayout extends CoreLayout<LayoutHint> {
    /**
     * The id of the currently displayed widget
     */
    @LayoutConfig
    private String defaultCard;

    /**
     * Maps ids to their corresponding widgets
     */
    private Map<String, UIWidget> widgets = Maps.newHashMap();

    /**
     * The default constructor
     */
    public CardLayout() {

    }

    /**
     * The parameterized constructor
     *
     * @param id the id assigned to the {@code CardLayout}
     */
    public CardLayout(String id) {
        super(id);
    }

    /**
     * Adds the widget to the list of widgets stored in the {@code CardLayout}
     *
     * @param widget The {@link UIWidget} to be added to the {@code CardLayout}
     * @throws IllegalArgumentException if the widget does not have an id
     */
    public void addWidget(UIWidget widget) {
        String id = widget.getId();
        if (id == null) {
            throw new IllegalArgumentException("CardLayout requires for each widget to be added to it to have an id");
        }
        widgets.put(id, widget);
    }

    /**
     * Removes the widget from the list of widgets stored in the {@code CardLayout}
     *
     * @param widget The {@code UIWidget} to be removed from the {@code CardLayout}
     */
    @Override
    public void removeWidget(UIWidget widget) {
        String id = widget.getId();
        if (id != null) {
            widgets.remove(id);
        }
    }

    /**
     * Sets the currently displayed widget
     *
     * @param id The id of the {@code UIWidget} to be displayed
     */
    public void setDisplayedCard(String id) {
        defaultCard = id;
    }

    /**
     * Adds the widget to the list of widgets stored in the {@code CardLayout}
     *
     * @param element The {@code UIWidget} to add
     * @param hint A hint as to how the widget should be laid out - may be null (and null values should be handled).
     */
    @Override
    public void addWidget(UIWidget element, LayoutHint hint) {
        addWidget(element);
    }

    /**
     * Handles how the {@code CardLayout} is drawn - called every frame
     *
     * @param canvas The {@link Canvas} on which the {@code CardLayout} is drawn
     */
    @Override
    public void onDraw(Canvas canvas) {
        if (defaultCard != null) {
            UIWidget widget = widgets.get(defaultCard);
            if (widget != null) {
                canvas.drawWidget(widget);
            }
        }
    }

    /**
     * Retrieves the preferred content size of the {@code CardLayout}
     *
     * @param canvas The {@code Canvas} on which the {@code CardLayout} is drawn
     * @param sizeHint A hint as to how the {@code CardLayout} should be laid out
     * @return A {@link Vector2i} representing the preferred content size of the {@code CardLayout}
     */
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

    /**
     * Retrieves the maximum content size of the {@code CardLayout}
     *
     * @param canvas The {@code Canvas} on which the {@code CardLayout} is drawn
     * @return A {@code Vector2i} representing the maximum content size of the {@code CardLayout}
     */
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

    /**
     * Retrieves an {@link Iterator} containing the list of widgets stored in the {@code CardLayout}
     *
     * @return The {@code Iterator} containing the list of {@code UIWidgets}
     */
    @Override
    public Iterator<UIWidget> iterator() {
        return widgets.values().iterator();
    }
}
