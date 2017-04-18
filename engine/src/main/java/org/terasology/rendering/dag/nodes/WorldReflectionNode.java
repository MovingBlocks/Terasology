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
package org.terasology.rendering.dag.nodes;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.LookThrough;
import org.terasology.rendering.dag.stateChanges.ReflectedCamera;
import org.terasology.rendering.dag.stateChanges.SetFacesToCull;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.chunks.RenderableChunk;

import static org.terasology.rendering.opengl.ScalingFactors.HALF_SCALE;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.terasology.rendering.primitives.ChunkMesh.RenderPhase.OPAQUE;

/**
 * An instance of this class is responsible for rendering a reflected landscape into the
 * "engine:sceneReflected" buffer. This buffer is then used to produce the reflection
 * of the landscape on the water surface.
 *
 * It could potentially be used also for other reflecting surfaces, i.e. metal, but it only works
 * for horizontal surfaces.
 *
 * An instance of this class is enabled or disabled depending on the reflections setting in the rendering config.
 *
 * Diagram of this node can be viewed from:
 * TODO: move diagram to the wiki when this part of the code is stable
 * - https://docs.google.com/drawings/d/1Iz7MA8Y5q7yjxxcgZW-0antv5kgx6NYkvoInielbwGU/edit?usp=sharing
 */
public class WorldReflectionNode extends ConditionDependentNode {
    public static final ResourceUrn REFLECTED_FBO = new ResourceUrn("engine:sceneReflected");
    private static final ResourceUrn CHUNK_MATERIAL = new ResourceUrn("engine:prog.chunk");

    private RenderQueuesHelper renderQueues;
    private WorldRenderer worldRenderer;

    private Camera playerCamera;
    private Material chunkMaterial;
    private RenderingConfig renderingConfig;

    /**
     * Constructs an instance of this class.
     *
     * Internally requires the "engine:sceneReflected" buffer, stored in the (display) resolution-dependent FBO manager.
     * This is a default, half-scale buffer inclusive of a depth buffer FBO. See FBOConfig and ScalingFactors for details
     * on possible FBO configurations.
     *
     * This method also requests the material using the "chunk" shaders (vertex, fragment) to be enabled.
     */
    public WorldReflectionNode(Context context) {
        super(context);

        renderQueues = context.get(RenderQueuesHelper.class);

        worldRenderer = context.get(WorldRenderer.class);
        playerCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new ReflectedCamera(playerCamera)); // this has to go before the LookThrough state change
        addDesiredStateChange(new LookThrough(playerCamera));

        renderingConfig = context.get(Config.class).getRendering();
        requiresCondition(() -> renderingConfig.isReflectiveWater());
        renderingConfig.subscribe(RenderingConfig.REFLECTIVE_WATER, this);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        requiresFBO(new FBOConfig(REFLECTED_FBO, HALF_SCALE, FBO.Type.DEFAULT).useDepthBuffer(), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(REFLECTED_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(REFLECTED_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new EnableFaceCulling());
        addDesiredStateChange(new SetFacesToCull(GL_FRONT));
        addDesiredStateChange(new EnableMaterial(CHUNK_MATERIAL));

        // we must get this here because in process we activate/deactivate a specific shader feature.
        // TODO: improve EnableMaterial to take advantage of shader feature bitmasks.
        chunkMaterial = getMaterial(CHUNK_MATERIAL);
    }

    /**
     * Renders the landscape, reflected, into the buffers attached to the "engine:sceneReflected" FBO. It is used later,
     * to render horizontal reflective surfaces, i.e. water.
     *
     * Notice that this method -does not- clear the FBO. The rendering takes advantage of the depth buffer to decide
     * which pixel is in front of the one already stored in the buffer.
     *
     * See: https://en.wikipedia.org/wiki/Deep_image_compositing
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/worldReflection");

        int numberOfRenderedTriangles = 0;
        int numberOfChunksThatAreNotReadyYet = 0;

        final Vector3f cameraPosition = playerCamera.getPosition();

        chunkMaterial.activateFeature(ShaderProgramFeature.FEATURE_USE_FORWARD_LIGHTING);
        chunkMaterial.setFloat("clip", playerCamera.getClipHeight(), true);

        while (renderQueues.chunksOpaqueReflection.size() > 0) {
            RenderableChunk chunk = renderQueues.chunksOpaqueReflection.poll();

            if (chunk.hasMesh()) {
                final ChunkMesh chunkMesh = chunk.getMesh();
                final Vector3f chunkPosition = chunk.getPosition().toVector3f();

                chunkMesh.updateMaterial(chunkMaterial, chunkPosition, chunk.isAnimated());
                numberOfRenderedTriangles += chunkMesh.render(OPAQUE, chunkPosition, cameraPosition);

            } else {
                numberOfChunksThatAreNotReadyYet++;
            }
        }

        chunkMaterial.deactivateFeature(ShaderProgramFeature.FEATURE_USE_FORWARD_LIGHTING);

        worldRenderer.increaseTrianglesCount(numberOfRenderedTriangles);
        worldRenderer.increaseNotReadyChunkCount(numberOfChunksThatAreNotReadyYet);

        PerformanceMonitor.endActivity();
    }
}
