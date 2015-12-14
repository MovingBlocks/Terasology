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
package org.terasology.rendering.nui;

import org.terasology.input.BindButtonEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.skin.UISkin;

import java.util.Collection;

/**
 */
public interface UIWidget extends Iterable<UIWidget> {

    String DEFAULT_MODE = "";
    String HOVER_MODE = "hover";
    String FOCUSED_MODE = "focused";
    String ACTIVE_MODE = "active";
    String BASE_PART = "base";

    String getId();

    UISkin getSkin();

    void setSkin(UISkin skin);

    String getFamily();

    void setFamily(String family);

    void bindFamily(Binding<String> binding);

    String getMode();

    /**
     * @return Whether the widget is currently visible and should be rendered
     */
    boolean isVisible();

    /**
     * Finds a widget with the given id and type, within the current widget and its contents.
     *
     * @param id
     * @param type
     * @param <T>
     * @return The widget with the given id and type, or null.
     */
    <T extends UIWidget> T find(String id, Class<T> type);

    <T extends UIWidget> Collection<T> findAll(Class<T> type);

    void onDraw(Canvas canvas);

    void update(float delta);

    void onGainFocus();

    void onLoseFocus();

    void onMouseButtonEvent(MouseButtonEvent event);

    void onMouseWheelEvent(MouseWheelEvent event);


    /**
     *
     * @return Whether the input should be consumed, and thus not propagated to other interaction regions
     */
    boolean onKeyEvent(NUIKeyEvent event);

    void onBindEvent(BindButtonEvent event);

    Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint);

    Vector2i getMaxContentSize(Canvas canvas);

    boolean isSkinAppliedByCanvas();

    boolean canBeFocus();

    void bindTooltip(Binding<UIWidget> bind);

    void setTooltip(UIWidget value);

    UIWidget getTooltip();

    void bindTooltipString(Binding<String> bind);

    void setTooltip(String value);

    float getTooltipDelay();

}
