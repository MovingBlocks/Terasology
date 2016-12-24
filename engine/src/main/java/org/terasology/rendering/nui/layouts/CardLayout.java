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
    private String currentlyDisplayedCard;

    /**
     * Maps ids to their corresponding widgets
     */
    private Map<String, UIWidget> widgets = Maps.newHashMap();

    /**
     * Creates an empty {@code CardLayout}
     */
    public CardLayout() {

    }

    /**
     * Creates an empty {@code CardLayout} with the given id.
     *
     * @param id The id assigned to this {@code CardLayout}
     */
    public CardLayout(String id) {
        super(id);
    }

    /**
     * Adds the widget to this layout's widget list.
     *
     * @param widget The {@link UIWidget} to be added to this {@code CardLayout}
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
     * Adds the widget to this layout's widget list.
     *
     * @param element The {@code UIWidget} to add
     * @param hint (unused)
     */
    @Override
    public void addWidget(UIWidget element, LayoutHint hint) {
        addWidget(element);
    }

    /**
     * Removes the widget from this layout's widget list.
     *
     * @param widget The {@code UIWidget} to be removed from this {@code CardLayout}
     */
    @Override
    public void removeWidget(UIWidget widget) {
        String id = widget.getId();
        if (id != null) {
            widgets.remove(id);
        }
    }

    /**
     * Sets the currently displayed widget.
     *
     * @param id The id of the {@code UIWidget} to be displayed
     */
    public void setDisplayedCard(String id) {
        currentlyDisplayedCard = id;
    }

    /**
     * Draws the currently displayed widget, as defined in {@code currentDisplayedCard}.
     * This is called every frame.
     *
     * @param canvas The {@link Canvas} on which this {@code CardLayout} is drawn
     */
    @Override
    public void onDraw(Canvas canvas) {
        if (currentlyDisplayedCard != null) {
            UIWidget widget = widgets.get(currentlyDisplayedCard);
            if (widget != null) {
                canvas.drawWidget(widget);
            }
        }
    }

    /**
     * Retrieves the preferred content size of this {@code CardLayout}.
     * This is the minimum size this layout will take, given no space restrictions.
     *
     * @param canvas The {@code Canvas} on which this {@code CardLayout} is drawn
     * @param sizeHint A {@link Vector2i} representing the space available for widgets to be drawn in this layout
     * @return A {@code Vector2i} representing the preferred content size of this {@code CardLayout}
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
     * Retrieves the maximum content size of this {@code CardLayout}.
     *
     * @param canvas The {@code Canvas} on which this {@code CardLayout} is drawn
     * @return A {@code Vector2i} representing the maximum content size of this {@code CardLayout}
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
     * Retrieves an {@link Iterator} containing this layout's widget list.
     *
     * @return An {@code Iterator} containing the list of {@code UIWidgets}
     */
    @Override
    public Iterator<UIWidget> iterator() {
        return widgets.values().iterator();
    }
}
