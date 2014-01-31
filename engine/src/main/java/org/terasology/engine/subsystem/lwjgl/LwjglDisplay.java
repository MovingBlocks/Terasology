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

import static org.lwjgl.opengl.GL11.glViewport;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.registry.CoreRegistry;

public class LwjglDisplay implements org.terasology.engine.subsystem.Display {

    private static final Logger logger = LoggerFactory.getLogger(LwjglDisplay.class);

    public LwjglDisplay() {
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
            logger.error("Can not initialize graphics device.", e);
            System.exit(1);
        }
    }

    @Override
    public void resizeViewport() {
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    @Override
    public boolean wasResized() {
        return Display.wasResized();
    }

    @Override
    public void processMessages() {
        Display.processMessages();
    }
}
