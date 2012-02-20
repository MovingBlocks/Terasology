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

import org.lwjgl.opengl.Display;
import org.terasology.logic.manager.ShaderManager;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Base class for all displayable UI elements.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class UIDisplayElement {

    private boolean _visible = false;

    private final Vector2f _position = new Vector2f(0, 0);
    private final Vector2f _size = new Vector2f(1, 1);

    protected boolean _clickSoundPlayed = false;
    protected boolean _mouseDown = false, _mouseUp = false;

    private boolean _overlay;

    private UIDisplayElement _parent;

    public UIDisplayElement() {
    }

    public UIDisplayElement(Vector2f position) {
        _position.set(position);
    }

    public UIDisplayElement(Vector2f position, Vector2f size) {
        _position.set(position);
        _size.set(size);
    }

    public void renderTransformed() {
        ShaderManager.getInstance().enableDefault();

        if (isVisible()) {
            glPushMatrix();
            glTranslatef(getPosition().x, getPosition().y, 0);
            render();
            glPopMatrix();
        }
    }

    public void processKeyboardInput(int key) {
        // Nothing to do here
    }

    public void processMouseInput(int button, boolean state, int wheelMoved) {
        if (button == 0 && state && !_mouseUp) {
            _mouseDown = true;
            _mouseUp = false;
            _clickSoundPlayed = false;
        } else if (button == 0 && !state && _mouseDown) {
            _mouseUp = true;
            _mouseDown = false;
        }
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

    /**
     * Returns true if the given point intersects the display element.
     *
     * @param point The point to test
     * @return True if intersecting
     */
    public boolean intersects(Vector2f point) {
        return (point.x >= calcAbsolutePosition().x && point.y >= calcAbsolutePosition().y && point.x <= calcAbsolutePosition().x + getSize().x && point.y <= calcAbsolutePosition().y + getSize().y);
    }

    /**
     * Calculates the center position on the screen based on the active resolution and size of the display element.
     *
     * @return The center position
     */
    public Vector2f calcCenterPosition() {
        return new Vector2f(Display.getWidth() / 2 - getSize().x / 2, Display.getHeight() / 2 - getSize().y / 2);
    }

    public void center() {
        getPosition().set(calcCenterPosition());
    }

    public void centerVertically() {
        getPosition().y = calcCenterPosition().y;
    }

    public void centerHorizontally() {
        getPosition().x = calcCenterPosition().x;
    }

    public Vector2f calcAbsolutePosition() {
        if (_parent == null) {
            return getPosition();
        } else {
            return new Vector2f(_parent.calcAbsolutePosition().x + getPosition().x, _parent.calcAbsolutePosition().y + getPosition().y);
        }
    }

    public boolean isOverlay() {
        return _overlay;
    }

    public void setOverlay(boolean value) {
        _overlay = value;
    }
}
