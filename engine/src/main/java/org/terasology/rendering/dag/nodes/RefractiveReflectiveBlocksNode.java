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
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;

import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;

import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.primitives.ChunkMesh.RenderPhase.REFRACTIVE;

import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.chunks.RenderableChunk;

/**
 * This node renders refractive/reflective blocks, i.e. water blocks.
 *
 * Reflections always include the sky but may or may not include the landscape,
 * depending on the "Reflections" video setting. Any other object currently
 * reflected is an artifact.
 *
 * Refractions distort the blocks behind the refracting surface, i.e. the bottom
 * of a lake seen from above water or the landscape above water when the player is underwater.
 * Refractions are currently always enabled.
 *
 * Note: a third "Reflections" video setting enables Screen-space Reflections (SSR),
 * an experimental feature. It produces initially appealing reflections but rotating the
 * camera partially spoils the effect showing its limits.
 */
public class RefractiveReflectiveBlocksNode extends AbstractNode implements FBOManagerSubscriber {
    public static final ResourceUrn REFRACTIVE_REFLECTIVE = new ResourceUrn("engine:sceneReflectiveRefractive");
    private static final ResourceUrn CHUNK_SHADER = new ResourceUrn("engine:prog.chunk");

    @In
    private RenderQueuesHelper renderQueues;

    @In
    private WorldRenderer worldRenderer;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private Camera playerCamera;
    private Material chunkShader;

    /**
     * Initialises the node. -Must- be called once after instantiation.
     */
    @Override
    public void initialise() {
        playerCamera = worldRenderer.getActiveCamera();
        displayResolutionDependentFBOs.subscribe(this);
        requiresFBO(new FBOConfig(REFRACTIVE_REFLECTIVE, FULL_SCALE, FBO.Type.HDR).useNormalBuffer(), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(REFRACTIVE_REFLECTIVE, displayResolutionDependentFBOs));
        addDesiredStateChange(new EnableMaterial(CHUNK_SHADER.toString()));
        chunkShader = getMaterial(CHUNK_SHADER);
    }

    /**
     * This method is where the actual rendering of refractive/reflective blocks takes place.
     *
     * Also takes advantage of the two methods
     *
     * - WorldRenderer.increaseTrianglesCount(int)
     * - WorldRenderer.increaseNotReadyChunkCount(int)
     *
     * to publish some statistics over its own activity.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/RefractiveReflectiveBlocks");

        int numberOfRenderedTriangles = 0;
        int numberOfChunksThatAreNotReadyYet = 0;

        final Vector3f cameraPosition = playerCamera.getPosition();
        playerCamera.lookThrough(); // TODO: remove. Placed here to make the dependency explicit.

        chunkShader.activateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);
        chunkShader.setFloat("clip", 0.0f, true);

        while (renderQueues.chunksAlphaBlend.size() > 0) {
            RenderableChunk chunk = renderQueues.chunksAlphaBlend.poll();

            if (chunk.hasMesh()) {
                final ChunkMesh chunkMesh = chunk.getMesh();
                final Vector3f chunkPosition = chunk.getPosition().toVector3f();

                chunkMesh.updateMaterial(chunkShader, chunkPosition, chunk.isAnimated());
                numberOfRenderedTriangles += chunkMesh.render(REFRACTIVE, chunkPosition, cameraPosition);

            } else {
                numberOfChunksThatAreNotReadyYet++;
            }
        }

        chunkShader.deactivateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);

        worldRenderer.increaseTrianglesCount(numberOfRenderedTriangles);
        worldRenderer.increaseNotReadyChunkCount(numberOfChunksThatAreNotReadyYet);

        PerformanceMonitor.endActivity();
    }

    @Override
    public void update() {
        // TODO: renames, maybe?
        READ_ONLY_GBUFFER.attachDepthBufferTo(displayResolutionDependentFBOs.get(REFRACTIVE_REFLECTIVE));
    }
}
