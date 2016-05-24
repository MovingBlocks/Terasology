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
package org.terasology.rendering.world.dag;

import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.OrthographicCamera;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.WorldRendererImpl;

public class ShadowMapNode extends RenderableNode {

    private static final int SHADOW_FRUSTUM_BOUNDS = 500;
    public final Camera camera = new OrthographicCamera(-SHADOW_FRUSTUM_BOUNDS, SHADOW_FRUSTUM_BOUNDS, SHADOW_FRUSTUM_BOUNDS, -SHADOW_FRUSTUM_BOUNDS);
    public Material shader;
    public boolean isFirstRenderingStageForCurrentFrame;


    @Override
    public void initialise() {


    }

    @Override
    public void render() {

        if (renderer.config.isDynamicShadows() && isFirstRenderingStageForCurrentFrame) {
            PerformanceMonitor.startActivity("Render World (Shadow Map)");

            renderer.graphicState.preRenderSetupSceneShadowMap();
            camera.lookThrough();

            while (renderer.queues.chunksOpaqueShadow.size() > 0) {
                renderer.renderChunk(renderer.queues.chunksOpaqueShadow.poll(), ChunkMesh.RenderPhase.OPAQUE, camera, WorldRendererImpl.ChunkRenderMode.SHADOW_MAP);
            }

            for (RenderSystem system : renderer.systemManager.iterateRenderSubscribers()) {
                system.renderShadows();
            }

            renderer.playerCamera.lookThrough(); // not strictly needed: just defensive programming here.
            renderer.graphicState.postRenderCleanupSceneShadowMap();

            PerformanceMonitor.endActivity();
        }
    }

    public void positionShadowMapCamera() {
        // Shadows are rendered around the player so...
        Vector3f lightPosition = new Vector3f(renderer.playerCamera.getPosition().x, 0.0f, renderer.playerCamera.getPosition().z);

        // Project the camera position to light space and make sure it is only moved in texel steps (avoids flickering when moving the camera)
        float texelSize = 1.0f / renderer.config.getShadowMapResolution();
        texelSize *= 2.0f;

        camera.getViewProjectionMatrix().transformPoint(lightPosition);
        lightPosition.set(TeraMath.fastFloor(lightPosition.x / texelSize) * texelSize, 0.0f, TeraMath.fastFloor(lightPosition.z / texelSize) * texelSize);
        camera.getInverseViewProjectionMatrix().transformPoint(lightPosition);

        // ... we position our new camera at the position of the player and move it
        // quite a bit into the direction of the sun (our main light).

        // Make sure the sun does not move too often since it causes massive shadow flickering (from hell to the max)!
        float stepSize = 50f;
        Vector3f sunDirection = renderer.backdropProvider.getQuantizedSunDirection(stepSize);

        Vector3f sunPosition = new Vector3f(sunDirection);
        sunPosition.scale(256.0f + 64.0f);
        lightPosition.add(sunPosition);

        camera.getPosition().set(lightPosition);

        // and adjust it to look from the sun direction into the direction of our player
        Vector3f negSunDirection = new Vector3f(sunDirection);
        negSunDirection.scale(-1.0f);

        camera.getViewingDirection().set(negSunDirection);
    }
}
