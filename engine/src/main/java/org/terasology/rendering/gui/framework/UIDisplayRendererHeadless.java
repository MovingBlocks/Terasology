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

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2f;

import org.terasology.rendering.gui.widgets.UIWindow;

public class UIDisplayRendererHeadless implements UIDisplayRenderer {

    List<UIDisplayElement> displayElementList = new ArrayList<UIDisplayElement>();

    private UIWindow window;

    @Override
    public UIWindow getWindowFocused() {
        return window;
    }

    @Override
    public void setWindowFocus(UIWindow window) {
        this.window = window;
    }

    @Override
    public List<UIDisplayElement> getDisplayElements() {
        return displayElementList;
    }

    @Override
    public void addDisplayElementToPosition(int position, UIDisplayElement element) {
        displayElementList.add(position, element);
    }

    @Override
    public void removeDisplayElement(UIDisplayElement displayElement) {
        displayElementList.remove(displayElement);
    }

    @Override
    public void removeAllDisplayElements() {
        displayElementList.clear();
    }

    @Override
    public void renderTransformed() {
        // Do nothing
    }

    @Override
    public void render() {
        // Do nothing
    }

    @Override
    public void layout() {
        // Do nothing
    }

    @Override
    public void update() {
        // Do nothing
    }

    @Override
    public void setVisible(boolean b) {
        // Do nothing?
    }

    @Override
    public void setSize(Vector2f vector2f) {
        // Do nothing?
    }
}
