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
    public void loadProjectionMatrix() {
    }

    @Override
    public void loadNormalizedModelViewMatrix() {
    }

    @Override
    public void loadModelViewMatrix() {
    }

    @Override
    public boolean isBobbingAllowed() {
        return false;
    }
}
