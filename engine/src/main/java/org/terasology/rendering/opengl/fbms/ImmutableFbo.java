/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.opengl.fbms;

import org.terasology.engine.SimpleUri;
import org.terasology.rendering.opengl.AbstractFboManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FboConfig;

/**
 * Instances of this class manage Frame buffer Objects (FBOs) whose characteristics are immutable.
 *
 * Once an FBO is first generated through the request(FboConfig) method, it stays the same until it is disposed.
 */
public class ImmutableFbo extends AbstractFboManager {

    @Override
    public FBO request(FboConfig fboConfig) {
        FBO fbo;
        SimpleUri fboName = fboConfig.getName();
        if (fboConfigs.containsKey(fboName)) {
            if (!fboConfig.equals(fboConfigs.get(fboName))) {
                throw new IllegalArgumentException("Requested FBO is already available with different configuration");
            }
            fbo = fboLookup.get(fboConfig.getName());
        } else {
            fbo = generateWithDimensions(fboConfig, fboConfig.getDimensions());
        }
        retain(fboName);
        return fbo;
    }
}
