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
package org.terasology.rendering.nui;

import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.databinding.Binding;

/**
 * @author Immortius
 */
public interface UIWidget extends Iterable<UIWidget> {

    String DEFAULT_MODE = "";
    String HOVER_MODE = "hover";
    String ACTIVE_MODE = "active";

    String getId();

    String getFamily();

    void setFamily(String family);

    void bindFamily(Binding<String> binding);

    String getMode();

    boolean isVisible();

    <T extends UIWidget> T find(String id, Class<T> type);

    void onDraw(Canvas canvas);

    void update(float delta);

    void onGainFocus();

    void onLoseFocus();

    void onMouseButtonEvent(MouseButtonEvent event);

    void onMouseWheelEvent(MouseWheelEvent event);

    void onKeyEvent(KeyEvent event);

    Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint);

    Vector2i getMaxContentSize(Canvas canvas);

    boolean isSkinAppliedByCanvas();

}
