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

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.GameState;

import java.awt.*;

public class LwjglPortlet extends BaseLwjglSubsystem {

    private static final Logger logger = LoggerFactory.getLogger(LwjglPortlet.class);

    private Canvas customViewPort;

    @Override
    public String getName() {
        return "Portlet";
    }

    @Override
    public void postInitialise(Context context) {
        try {
            Display.setParent(customViewPort);
        } catch (LWJGLException e) {
            throw new RuntimeException("Can not initialize graphics device.", e);
        }
    }

    public void setCustomViewport(Canvas canvas) {
        this.customViewPort = canvas;
    }

}
