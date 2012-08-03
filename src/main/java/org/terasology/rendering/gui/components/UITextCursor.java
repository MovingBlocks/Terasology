/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.components;


import org.terasology.rendering.gui.framework.UIDisplayElement;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.*;

/**
 * A simple graphical text cursor
 *
 * @author Anton Kireev <adeon.k87@gmail.com>
 */
public class UITextCursor extends UIDisplayElement {

    public UITextCursor(){
        setSize(new Vector2f(2f,15f));
    }

    public void render() {
        glPushMatrix();
        glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
        glBegin(GL_QUADS);
        glVertex2f(getPosition().x, getPosition().y);
        glVertex2f(getPosition().x + 2f, getPosition().y);
        glVertex2f(getPosition().x + 2f, getPosition().y + 15f);
        glVertex2f(getPosition().x, getPosition().y + 15f);
        glEnd();
        glPopMatrix();
    }

    public void update() {

    }

    public void setPositionBySymbol() {
        //setPosition();
    }

	@Override
	public void layout() {

	}
}
