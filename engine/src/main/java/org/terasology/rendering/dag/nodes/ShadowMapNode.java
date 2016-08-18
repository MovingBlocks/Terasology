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
package org.terasology.rendering.dag.nodes;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.OrthographicCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.SetViewportSizeOf;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.ShadowMapResolutionDependentFBOs;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.RenderableWorld;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.WorldRendererImpl;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;


/**
 * Diagram of this node can be viewed from:
 * TODO: move diagram to the wiki when this part of the code is stable
 * - https://docs.google.com/drawings/d/13I0GM9jDFlZv1vNrUPlQuBbaF86RPRNpVfn5q8Wj2lc/edit?usp=sharing
 */
public class ShadowMapNode extends AbstractNode {
    public static final ResourceUrn SHADOW_MAP_URN = new ResourceUrn("engine:sceneShadowMap");
    private static final int SHADOW_FRUSTUM_BOUNDS = 500;
    public Camera shadowMapCamera = new OrthographicCamera(-SHADOW_FRUSTUM_BOUNDS, SHADOW_FRUSTUM_BOUNDS, SHADOW_FRUSTUM_BOUNDS, -SHADOW_FRUSTUM_BOUNDS);

    @In
    private RenderableWorld renderableWorld;

    @In
    private RenderQueuesHelper renderQueues;

    @In
    private Config config;

    @In
    private WorldRenderer worldRenderer;

    @In
    private BackdropProvider backdropProvider;

    @In
    private ShadowMapResolutionDependentFBOs shadowMapResolutionDependentFBOs;

    private Material shadowMapShader;
    private RenderingConfig renderingConfig;
    private Camera playerCamera;
    private float texelSize;
    private float stepSize;


    @Override
    public void initialise() {
        this.playerCamera = worldRenderer.getActiveCamera();
        this.shadowMapShader = worldRenderer.getMaterial("engine:prog.shadowMap");
        this.renderingConfig = config.getRendering();
        renderableWorld.setShadowMapCamera(shadowMapCamera);

        requireFBO(new FBOConfig(SHADOW_MAP_URN, FBO.Type.NO_COLOR).useDepthBuffer(), shadowMapResolutionDependentFBOs);

        // TODO: fix them
        addDesiredStateChange(new BindFBO(SHADOW_MAP_URN, shadowMapResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportSizeOf(SHADOW_MAP_URN, shadowMapResolutionDependentFBOs));
    }

    @Override
    public void process() {
        positionShadowMapCamera();

        // TODO: find an elegant way to fetch isFirstRenderingStageForCurrentFrame
        // TODO: check these conditions before adding this node inside the DAG, removing this condition completely from process()
        if (renderingConfig.isDynamicShadows() && worldRenderer.isFirstRenderingStageForCurrentFrame()) {
            PerformanceMonitor.startActivity("rendering/shadowMap");

            // preRenderSetupSceneShadowMap
            // TODO: verify the need to clear color buffer
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            GL11.glDisable(GL_CULL_FACE);

            // render
            shadowMapCamera.lookThrough();

            shadowMapShader.enable();
            // FIXME: storing chunksOpaqueShadow or a mechanism for requesting a chunk queue for nodes which calls renderChunks method?
            worldRenderer.renderChunks(renderQueues.chunksOpaqueShadow, ChunkMesh.RenderPhase.OPAQUE, shadowMapCamera, WorldRendererImpl.ChunkRenderMode.SHADOW_MAP);
            playerCamera.lookThrough(); //FIXME: not strictly needed: just defensive programming here.

            // postRenderCleanupSceneShadowMap
            GL11.glEnable(GL_CULL_FACE);


            PerformanceMonitor.endActivity();
        }
    }

    private void positionShadowMapCamera() {
        // Shadows are rendered around the player so...
        Vector3f lightPosition = new Vector3f(playerCamera.getPosition().x, 0.0f, playerCamera.getPosition().z);

        // Project the shadowMapCamera position to light space and make sure it is only moved in texel steps (avoids flickering when moving the shadowMapCamera)
        texelSize = 1.0f / renderingConfig.getShadowMapResolution();
        texelSize *= 2.0f;

        shadowMapCamera.getViewProjectionMatrix().transformPoint(lightPosition);
        lightPosition.set(TeraMath.fastFloor(lightPosition.x / texelSize) * texelSize, 0.0f, TeraMath.fastFloor(lightPosition.z / texelSize) * texelSize);
        shadowMapCamera.getInverseViewProjectionMatrix().transformPoint(lightPosition);

        // ... we position our new shadowMapCamera at the position of the player and move it
        // quite a bit into the direction of the sun (our main light).

        // Make sure the sun does not move too often since it causes massive shadow flickering (from hell to the max)!
        stepSize = 50f;
        Vector3f sunDirection = backdropProvider.getQuantizedSunDirection(stepSize);

        Vector3f sunPosition = new Vector3f(sunDirection);
        sunPosition.scale(256.0f + 64.0f);
        lightPosition.add(sunPosition);
        shadowMapCamera.getPosition().set(lightPosition);

        // and adjust it to look from the sun direction into the direction of our player
        Vector3f negSunDirection = new Vector3f(sunDirection);
        negSunDirection.scale(-1.0f);

        shadowMapCamera.getViewingDirection().set(negSunDirection);
        shadowMapCamera.update(worldRenderer.getSecondsSinceLastFrame());
    }
}
