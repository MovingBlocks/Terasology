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
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;

/**
 * Basic shader parameters for all shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersBase implements ShaderParameters {

    public ShaderParametersBase() {
    }

    @Override
    public void initialParameters(Material material) {

    }

    @Override
    public void applyParameters(Material program) {

        program.setFloat("viewingDistance", CoreRegistry.get(Config.class).getRendering().getViewDistance().getChunkDistance().getX() * 8.0f);

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

        if (worldRenderer != null) {
            program.setFloat("daylight", worldRenderer.getDaylight(), true);
            program.setFloat("swimming", worldRenderer.isHeadUnderWater() ? 1.0f : 0.0f, true);
            program.setFloat("tick", worldRenderer.getTick(), true);
            program.setFloat("sunlightValueAtPlayerPos", worldRenderer.getSmoothedPlayerSunlightValue(), true);

            if (worldRenderer.getActiveCamera() != null) {
                final Vector3f cameraDir = worldRenderer.getActiveCamera().getViewingDirection();
                final Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

                program.setFloat3("cameraPosition", cameraPosition.x, cameraPosition.y, cameraPosition.z, true);
                program.setFloat3("cameraDirection", cameraDir.x, cameraDir.y, cameraDir.z, true);
                program.setFloat3("cameraParameters", worldRenderer.getActiveCamera().getzNear(), worldRenderer.getActiveCamera().getzFar(), 0.0f, true);
            }

            Vector3f sunDirection = worldRenderer.getSkysphere().getSunDirection(false);
            program.setFloat3("sunVec", sunDirection.x, sunDirection.y, sunDirection.z, true);
        }

        if (worldProvider != null) {
            program.setFloat("time", worldProvider.getTime().getDays());
        }
    }
}
