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
package org.terasology.rendering.gui.components;

import org.lwjgl.opengl.GL11;
import org.terasology.rendering.gui.framework.UIDisplayElement;

import static org.lwjgl.opengl.GL11.*;

/**
 * A simple crosshair.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UICrosshair extends UIDisplayElement {

    private static int _displayList = -1;

    public void render() {
        glLineWidth(2f);

        if (_displayList == -1)
            generateDisplayList();

        glCallList(_displayList);
    }

    private void generateDisplayList() {
        _displayList = glGenLists(1);

        glNewList(_displayList, GL11.GL_COMPILE);
        glBegin(GL_LINES);
        glColor3f(1.0f, 1.0f, 1.0f);
        glVertex2f(-8f, 0f);
        glVertex2f(8f, 0f);
        glVertex2f(0f, -8f);
        glVertex2f(0f, 8f);
        glEnd();
        glEndList();
    }

    @Override
    public void update() {
    }
}
