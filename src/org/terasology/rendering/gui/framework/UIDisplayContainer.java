/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import javax.vecmath.Vector2f;
import java.util.ArrayList;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.Display;
/**
 * Composition of multiple display elements.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class UIDisplayContainer extends UIDisplayElement {

    final ArrayList<UIDisplayElement> _displayElements = new ArrayList<UIDisplayElement>();
    private boolean _crop = false;

    public UIDisplayContainer() {
        super();
    }

    public UIDisplayContainer(Vector2f position) {
        super(position);
    }

    public UIDisplayContainer(Vector2f position, Vector2f size) {
        super(position, size);
    }

    public void render() {
        if (!isVisible())
            return;


        if(_crop){
            glEnable(GL_SCISSOR_TEST);
            glScissor((int)getPosition().x, 0, (int)getSize().x, Display.getHeight());
        }

        // Render all display elements
        for (int i = 0; i < _displayElements.size(); i++) {

            _displayElements.get(i).renderTransformed();
        }

        if(_crop){
            glDisable(GL_SCISSOR_TEST);
        }
    }

    public void update() {
        if (!isVisible())
            return;

        // Update all display elements
        for (int i = 0; i < _displayElements.size(); i++) {
            _displayElements.get(i).update();
        }
    }

    @Override
    public void processKeyboardInput(int key) {
        if (!isVisible())
            return;

        super.processKeyboardInput(key);

        // Pass the pressed key to all display elements
        for (int i = 0; i < _displayElements.size(); i++) {
            _displayElements.get(i).processKeyboardInput(key);
        }
    }

    @Override
    public void processMouseInput(int button, boolean state, int wheelMoved) {
        if (!isVisible())
            return;

        super.processMouseInput(button, state, wheelMoved);

        // Pass the mouse event to all display elements
        for (int i = 0; i < _displayElements.size(); i++) {
            _displayElements.get(i).processMouseInput(button, state, wheelMoved);
        }
    }

    public void addDisplayElement(UIDisplayElement element) {
        _displayElements.add(element);
        element.setParent(this);
    }

    public void removeDisplayElement(UIDisplayElement element) {
        _displayElements.remove(element);
        element.setParent(null);
    }

    public ArrayList<UIDisplayElement> getDisplayElements() {
        return _displayElements;
    }

    public void setCrop(boolean crop){
        _crop = crop;
    }
}
