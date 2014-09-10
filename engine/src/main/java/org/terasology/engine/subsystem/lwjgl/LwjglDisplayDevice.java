/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.engine.subsystem.lwjgl;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glViewport;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.registry.CoreRegistry;

public class LwjglDisplayDevice implements DisplayDevice {

    private static final Logger logger = LoggerFactory.getLogger(LwjglDisplayDevice.class);

    public LwjglDisplayDevice() {
    }

    @Override
    public boolean isActive() {
        return Display.isActive();
    }

    @Override
    public boolean isCloseRequested() {
        return Display.isCloseRequested();
    }

    @Override
    public void setFullscreen(boolean state) {
        setFullscreen(state, true);
    }

    void setFullscreen(boolean state, boolean resize) {
        try {
            if (state) {
                Display.setDisplayMode(Display.getDesktopDisplayMode());
                Display.setFullscreen(true);
            } else {
                Config config = CoreRegistry.get(Config.class);
                Display.setDisplayMode(config.getRendering().getDisplayMode());
                Display.setResizable(true);
            }
        } catch (LWJGLException e) {
            throw new RuntimeException("Can not initialize graphics device.", e);
        }
        if (resize) {
            glViewport(0, 0, Display.getWidth(), Display.getHeight());
        }
    }

    @Override
    public void processMessages() {
        Display.processMessages();
    }

    @Override
    public boolean isHeadless() {
        return false;
    }

    @Override
    public void prepareToRender() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();
    }
}
