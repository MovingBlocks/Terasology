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
package com.github.begla.blockmania.gui.components;

import com.github.begla.blockmania.gui.framework.UIDisplayElement;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.*;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UITransparentOverlay extends UIDisplayElement {

    @Override
    public void render() {
        glColor4f(0.0f, 0.0f, 0.0f, 0.8f);
        glBegin(GL11.GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(getSize().x, 0);
        glVertex2f(getSize().x, getSize().y);
        glVertex2f(0, getSize().y);
        glEnd();
    }

    @Override
    public void update() {
        setSize(new Vector2f((float) Display.getWidth(), (float) Display.getHeight()));
    }
}
