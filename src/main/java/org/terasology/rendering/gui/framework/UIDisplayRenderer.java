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
package org.terasology.rendering.gui.framework;

import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;
import java.util.Collections;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

/**
 * Extends UIDisplayContainer to transparently init. OpenGL for 2D rendering.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIDisplayRenderer extends UIDisplayContainer {

    public UIDisplayRenderer() {
        setSize(new Vector2f(Display.getWidth(), Display.getHeight()));
    }

    @Override
    public void renderTransformed() {
        render();
    }

    @Override
    public void render() {
        if (isVisible()) {
            glMatrixMode(GL_PROJECTION);
            glPushMatrix();
            glLoadIdentity();
            glOrtho(0, Display.getWidth(), Display.getHeight(), 0, -32, 32);
            glMatrixMode(GL_MODELVIEW);
            glPushMatrix();
            glLoadIdentity();

            glDisable(GL_CULL_FACE);
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            super.render();

            glDisable(GL_BLEND);
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);

            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);
        }
    }

    /**
     * Get the focused window. The focused window is the top element within the display elements array which is visible.
     *
     * @return The focused window.
     */
    public UIWindow getWindowFocused() {
        if (getDisplayElements().size() > 0) {

            for (int i = getDisplayElements().size() - 1; i >= 0; i--) {
                if (getDisplayElements().get(i).isVisible()) {
                    return (UIWindow) getDisplayElements().get(i);
                }
            }
        }

        return null;
    }

    /**
     * Set the given window to the top position of the display element array. Therefore the window will be focused.
     *
     * @param window The window to focus.
     */
    public void setWindowFocus(UIWindow window) {
        int windowPosition = getDisplayElements().indexOf(window);

        if (windowPosition != -1) {
            Collections.rotate(getDisplayElements().subList(windowPosition, getDisplayElements().size()), -1);
        }
    }
}
