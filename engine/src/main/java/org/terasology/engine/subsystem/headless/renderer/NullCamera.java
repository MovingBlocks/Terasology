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
package org.terasology.engine.subsystem.headless.renderer;

import org.terasology.rendering.cameras.Camera;

public final class NullCamera extends Camera {
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
