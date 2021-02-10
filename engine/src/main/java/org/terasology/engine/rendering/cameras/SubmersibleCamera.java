/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.cameras;

import org.joml.Vector3f;
import org.terasology.config.RenderingConfig;
import org.terasology.rendering.RenderHelper;
import org.terasology.world.WorldProvider;

public abstract class SubmersibleCamera extends Camera {

    /* Used for Underwater Checks */
    private WorldProvider worldProvider;
    RenderingConfig renderingConfig;

    public SubmersibleCamera(WorldProvider worldProvider, RenderingConfig renderingConfig) {
        this.worldProvider = worldProvider;
        this.renderingConfig = renderingConfig;
    }

    /**
     * Returns True if the head of the player is underwater. False otherwise.
     *
     * Takes in account waves if present.
     *
     * @return True if the head of the player is underwater. False otherwise.
     */
    public boolean isUnderWater() {
        // TODO: Making this as a subscribable value especially for node "ChunksRefractiveReflectiveNode",
        // TODO: glDisable and glEnable state changes on that node will be dynamically added/removed based on this value.
        Vector3f cameraPosition = new Vector3f(this.getPosition());

        // Compensate for waves
        if (renderingConfig.isAnimateWater()) {
            cameraPosition.y -= RenderHelper.evaluateOceanHeightAtPosition(cameraPosition, worldProvider.getTime().getDays());
        }

        if (worldProvider.isBlockRelevant(cameraPosition)) {
            return worldProvider.getBlock(cameraPosition).isLiquid();
        }
        return false;
    }
}
