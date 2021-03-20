// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.opengl.fbms;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.rendering.opengl.AbstractFboManager;
import org.terasology.engine.rendering.opengl.FBO;
import org.terasology.engine.rendering.opengl.FboConfig;

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
