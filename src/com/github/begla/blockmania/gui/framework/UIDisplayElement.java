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
package com.github.begla.blockmania.gui.framework;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Base class for all displayable UI elements.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class UIDisplayElement {

    private boolean _visible = false;

    private Vector2f _position = new Vector2f(0, 0);
    private Vector2f _size = new Vector2f(1, 1);

    private UIDisplayElement _parent;

    public UIDisplayElement() {
    }

    public UIDisplayElement(Vector2f position) {
        _position.set(position);
    }

    public UIDisplayElement(Vector2f position, Vector2f scale) {
        _position.set(position);
        _size.set(scale);
    }

    protected void renderElement() {
        if (isVisible()) {
            glPushMatrix();
            glTranslatef(getPosition().x, getPosition().y, 0);
            render();
            glPopMatrix();
        }
    }

    public void processKeyboardInput(int key) {

    }

    public void processMouseInput(int button, boolean state, int wheelMoved) {

    }

    public abstract void render();

    public abstract void update();

    public Vector2f getPosition() {
        return _position;
    }

    public void setPosition(Vector2f position) {
        _position.set(position);
    }

    public Vector2f getSize() {
        return _size;
    }

    public void setSize(Vector2f scale) {
        _size.set(scale);
    }

    public void setVisible(boolean visible) {
        _visible = visible;
    }

    public boolean isVisible() {
        return _visible;
    }

    public UIDisplayElement getParent() {
        return _parent;
    }

    public void setParent(UIDisplayElement parent) {
        _parent = parent;
    }

    public boolean intersects(Vector2f point) {
        return (point.x >= getPosition().x && point.y >= getPosition().y && point.x <= getPosition().x + getSize().x && point.y <= getPosition().y + getSize().y);
    }

}
