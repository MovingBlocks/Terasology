/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.gui.framework;

import java.util.List;

import javax.vecmath.Vector2f;

import org.terasology.rendering.gui.widgets.UIWindow;

public interface UIDisplayRenderer {

    public void renderTransformed();

    public void render();

    /**
     * Get the focused window. The focused window is the top element within the display elements array which is visible.
     *
     * @return The focused window.
     */
    public UIWindow getWindowFocused();

    /**
     * Set the given window to the top position of the display element array. Therefore the window will be focused.
     *
     * @param window The window to focus.
     */
    public void setWindowFocus(UIWindow window);

    public void addDisplayElementToPosition(int position, UIDisplayElement element);

    public List<UIDisplayElement> getDisplayElements();

    public void layout();

    public void removeAllDisplayElements();

    public void update();

    public void setVisible(boolean b);

    public void setSize(Vector2f vector2f);

    public void removeDisplayElement(UIDisplayElement displayElement);
}