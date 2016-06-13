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

import org.lwjgl.opengl.GL11;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.OrthographicCamera;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.WorldRendererImpl;
import static org.lwjgl.opengl.GL11.*;
import static org.terasology.rendering.opengl.OpenGLUtil.*;


/**
 * Diagram of this node can be viewed from:
 * TODO: move diagram to the wiki when this part of the code is stable
 * - https://docs.google.com/drawings/d/13I0GM9jDFlZv1vNrUPlQuBbaF86RPRNpVfn5q8Wj2lc/edit?usp=sharing
 */
public class ShadowMapNode implements Node {
    private static final int SHADOW_FRUSTUM_BOUNDS = 500;


    public Camera camera = new OrthographicCamera(-SHADOW_FRUSTUM_BOUNDS, SHADOW_FRUSTUM_BOUNDS, SHADOW_FRUSTUM_BOUNDS, -SHADOW_FRUSTUM_BOUNDS);
    private RenderQueuesHelper renderQueues;
    private Context context;
    private Material shadowMapShader;
    private FBO shadowMap;

    // TODO: every node proposes its modifiable configuration?
    private RenderingConfig renderingConfig;

    private Camera playerCamera;
    // FIXME: remove reference to WorldRendererImpl
    private WorldRenderer worldRenderer;
    private BackdropProvider backdropProvider;


    // FIXME: unnecessary arguments must be eliminated
    public ShadowMapNode(Context context, Material shadowMapShader, Camera playerCamera) {
        this.context = context;
        this.shadowMapShader = shadowMapShader;
        this.playerCamera = playerCamera;
        this.worldRenderer = context.get(WorldRenderer.class);
        this.backdropProvider = context.get(BackdropProvider.class);
        this.renderingConfig = context.get(Config.class).getRendering();
        this.shadowMap = context.get(FrameBuffersManager.class).getFBO("sceneShadowMap");
        this.renderQueues = context.get(RenderQueuesHelper.class);
    }


    @Override
    public void initialise() {
        // TODO: shader and fbo initialization here
        // TODO: using ShaderParametersShadowMap here, instead of storing in ShaderManagerLwjgl
    }

    @Override
    public void update(float deltaInSeconds) {
        this.shadowMap = context.get(FrameBuffersManager.class).getFBO("sceneShadowMap");

        // positionShadowMapCamera()

        // Shadows are rendered around the player so...
        Vector3f lightPosition = new Vector3f(playerCamera.getPosition().x, 0.0f, playerCamera.getPosition().z);

        // Project the shadowMapCamera position to light space and make sure it is only moved in texel steps (avoids flickering when moving the shadowMapCamera)
        float texelSize = 1.0f / renderingConfig.getShadowMapResolution();
        texelSize *= 2.0f;

        camera.getViewProjectionMatrix().transformPoint(lightPosition);
        lightPosition.set(TeraMath.fastFloor(lightPosition.x / texelSize) * texelSize, 0.0f, TeraMath.fastFloor(lightPosition.z / texelSize) * texelSize);
        camera.getInverseViewProjectionMatrix().transformPoint(lightPosition);

        // ... we position our new shadowMapCamera at the position of the player and move it
        // quite a bit into the direction of the sun (our main light).

        // Make sure the sun does not move too often since it causes massive shadow flickering (from hell to the max)!
        float stepSize = 50f;
        Vector3f sunDirection = backdropProvider.getQuantizedSunDirection(stepSize);

        Vector3f sunPosition = new Vector3f(sunDirection);
        sunPosition.scale(256.0f + 64.0f);
        lightPosition.add(sunPosition);

        camera.getPosition().set(lightPosition);

        // and adjust it to look from the sun direction into the direction of our player
        Vector3f negSunDirection = new Vector3f(sunDirection);
        negSunDirection.scale(-1.0f);

        camera.getViewingDirection().set(negSunDirection);

        camera.update(deltaInSeconds);

    }


    @Override
    public void process() {
        PerformanceMonitor.startActivity("Render World (Shadow Map)");

        // preRenderSetupSceneShadowMap
        shadowMap.bind();
        setViewportToSizeOf(shadowMap); // TODO: how about shadowMap.setAsViewport() ?
        // TODO: verify the need to clear color buffer
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL_CULL_FACE);

        // render
        camera.lookThrough();

        shadowMapShader.enable();
        // FIXME: storing chuncksOpaqueShadow or a mechanism for requesting a chunk queue for nodes which calls renderChunks method?
        worldRenderer.renderChunks(renderQueues.chunksOpaqueShadow, ChunkMesh.RenderPhase.OPAQUE, camera, WorldRendererImpl.ChunkRenderMode.SHADOW_MAP);
        playerCamera.lookThrough(); //FIXME: not strictly needed: just defensive programming here.


        // postRenderCleanupSceneShadowMap
        GL11.glEnable(GL_CULL_FACE);
        bindDisplay(); // bindDisplay()


        PerformanceMonitor.endActivity();
    }

    @Override
    public void postRender() {

    }

    @Override
    public void onDisposal() { // opposite of initialise
        // TODO: removing shader and fbo, whenever node is removed from DAG
    }

    @Override
    public void onRemoval() { // for explicit listeners

    }


    @Override
    public void onInsert() {
        // TODO: required initialization whenever node is inserted to DAG

    }

}
