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

import java.awt.Canvas;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.EngineSubsystem;

public class LwjglCustomViewPort implements EngineSubsystem {

    private static final Logger logger = LoggerFactory.getLogger(LwjglCustomViewPort.class);

    private Canvas customViewPort;

    @Override
    public void initializeEarly() {
    }

    @Override
    public void initializeLate(Config config) {
        try {
            Display.setParent(customViewPort);
        } catch (LWJGLException e) {
            logger.error("Can not initialize graphics device.", e);
            System.exit(1);
        }
    }

    @Override
    public void updateEarly(GameState currentState, float delta) {
    }

    @Override
    public void updateLate(GameState currentState, float delta) {
    }

    @Override
    public void shutdown(Config config) {
    }

    @Override
    public void dispose() {
    }

    public void setCustomViewport(Canvas canvas) {
        this.customViewPort = canvas;
    }

}
