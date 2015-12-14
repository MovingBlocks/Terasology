/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.shader;

import org.terasology.config.Config;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

/**
 * Basic shader parameters for all shader program.
 *
 */
public class ShaderParametersBase implements ShaderParameters {

    public ShaderParametersBase() {
    }

    @Override
    public void initialParameters(Material material) {

    }

    @Override
    public void applyParameters(Material program) {

        program.setFloat("viewingDistance", CoreRegistry.get(Config.class).getRendering().getViewDistance().getChunkDistance().x * 8.0f);

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        BackdropProvider backdropProvider = CoreRegistry.get(BackdropProvider.class);

        if (worldRenderer != null && backdropProvider != null) {
            program.setFloat("daylight", backdropProvider.getDaylight(), true);
            program.setFloat("swimming", worldRenderer.isHeadUnderWater() ? 1.0f : 0.0f, true);
            program.setFloat("tick", worldRenderer.getTick(), true);
            program.setFloat("sunlightValueAtPlayerPos", worldRenderer.getSmoothedPlayerSunlightValue(), true);

            Camera activeCamera = worldRenderer.getActiveCamera();
            if (activeCamera != null) {
                final Vector3f cameraDir = activeCamera.getViewingDirection();
                final Vector3f cameraPosition = activeCamera.getPosition();

                program.setFloat3("cameraPosition", cameraPosition.x, cameraPosition.y, cameraPosition.z, true);
                program.setFloat3("cameraDirection", cameraDir.x, cameraDir.y, cameraDir.z, true);
                program.setFloat3("cameraParameters", activeCamera.getzNear(), activeCamera.getzFar(), 0.0f, true);
            }

            Vector3f sunDirection = backdropProvider.getSunDirection(false);
            program.setFloat3("sunVec", sunDirection.x, sunDirection.y, sunDirection.z, true);
        }

        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
        if (worldProvider != null) {
            program.setFloat("time", worldProvider.getTime().getDays());
        }
    }
}
