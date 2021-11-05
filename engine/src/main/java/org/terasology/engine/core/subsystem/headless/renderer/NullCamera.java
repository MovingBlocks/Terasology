// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.renderer;

import org.terasology.engine.rendering.cameras.Camera;

public final class NullCamera extends Camera {
    public NullCamera() {
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
