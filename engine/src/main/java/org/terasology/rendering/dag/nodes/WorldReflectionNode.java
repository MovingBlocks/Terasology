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
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
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
    public static final ResourceUrn REFLECTED = new ResourceUrn("engine:sceneReflected");
    private static final ResourceUrn CHUNK_MATERIAL = new ResourceUrn("engine:prog.chunk");

    @In
    private RenderQueuesHelper renderQueues;

    @In
    private Config config;

    @In
    private WorldRenderer worldRenderer;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private Camera playerCamera;
    private Material chunkShader;
    private RenderingConfig renderingConfig;

    /**
     * Node initialization.
     *
     * Internally requires the "engine:sceneReflected" buffer, stored in the (display) resolution-dependent FBO manager.
     * This is a default, half-scale buffer inclusive of a depth buffer FBO. See FBOConfig and ScalingFactors for details
     * on possible FBO configurations.
     *
     * This method also requests the material using the "chunk" shaders (vertex, fragment) to be enabled.
     */
    @Override
    public void initialise() {
        playerCamera = worldRenderer.getActiveCamera();

        renderingConfig = config.getRendering();
        requiresCondition(() -> renderingConfig.isReflectiveWater());
        renderingConfig.subscribe(RenderingConfig.REFLECTIVE_WATER, this);

        requiresFBO(new FBOConfig(REFLECTED, HALF_SCALE, FBO.Type.DEFAULT).useDepthBuffer(), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(REFLECTED, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(REFLECTED, displayResolutionDependentFBOs));
        addDesiredStateChange(new EnableFaceCulling());
        addDesiredStateChange(new SetFacesToCull(GL_FRONT));
        addDesiredStateChange(new EnableMaterial(CHUNK_MATERIAL.toString()));

        // we must get this here because in process we activate/deactivate a specific shader feature.
        // TODO: improve EnableMaterial to take advantage of shader feature bitmasks.
        chunkShader = getMaterial(CHUNK_MATERIAL);
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

        playerCamera.setReflected(true);
        playerCamera.lookThrough(); // TODO: handle this trough a state change
        final Vector3f cameraPosition = playerCamera.getPosition();

        chunkShader.activateFeature(ShaderProgramFeature.FEATURE_USE_FORWARD_LIGHTING);
        chunkShader.setFloat("clip", playerCamera.getClipHeight(), true);

        while (renderQueues.chunksOpaqueReflection.size() > 0) {
            RenderableChunk chunk = renderQueues.chunksOpaqueReflection.poll();

            if (chunk.hasMesh()) {
                final ChunkMesh chunkMesh = chunk.getMesh();
                final Vector3f chunkPosition = chunk.getPosition().toVector3f();

                chunkMesh.updateMaterial(chunkShader, chunkPosition, chunk.isAnimated());
                numberOfRenderedTriangles += chunkMesh.render(OPAQUE, chunkPosition, cameraPosition);

            } else {
                numberOfChunksThatAreNotReadyYet++;
            }
        }

        chunkShader.deactivateFeature(ShaderProgramFeature.FEATURE_USE_FORWARD_LIGHTING);
        playerCamera.setReflected(false);

        worldRenderer.increaseTrianglesCount(numberOfRenderedTriangles);
        worldRenderer.increaseNotReadyChunkCount(numberOfChunksThatAreNotReadyYet);

        PerformanceMonitor.endActivity();
    }
}
