// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.renderer;

import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.rendering.cameras.SubmersibleCamera;
import org.terasology.engine.world.WorldProvider;

public final class NullCamera extends SubmersibleCamera {
    public NullCamera(WorldProvider worldProvider, RenderingConfig renderingConfig) {
        super(worldProvider, renderingConfig);
    }

    @Override
    public void updateMatrices(float fov) {
    }

    @Override
    public void updateMatrices() {
    }

    @Override
    public boolean isBobbingAllowed() {
        return false;
    }
}
