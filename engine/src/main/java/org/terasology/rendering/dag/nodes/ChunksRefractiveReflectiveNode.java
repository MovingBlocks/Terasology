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

import org.lwjgl.opengl.GL11;
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
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.RenderableChunk;

/**
 * TODO: Diagram of this node
 */
public class ChunksRefractiveReflectiveNode extends AbstractNode implements FBOManagerSubscriber {
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

    @Override
    public void initialise() {
        playerCamera = worldRenderer.getActiveCamera();
        displayResolutionDependentFBOs.subscribe(this);
        requiresFBO(new FBOConfig(REFRACTIVE_REFLECTIVE, FULL_SCALE, FBO.Type.HDR).useNormalBuffer(), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(REFRACTIVE_REFLECTIVE, displayResolutionDependentFBOs));
        addDesiredStateChange(new EnableMaterial(CHUNK_SHADER.toString()));
        chunkShader = getMaterial(CHUNK_SHADER);
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/chunksrefractivereflective");

        int numberOfRenderedTriangles = 0;
        int numberOfChunksThatAreNotReadyYet = 0;

        final Vector3f cameraPosition = playerCamera.getPosition();

        chunkShader.activateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);
        chunkShader.setFloat("clip", 0.0f, true);

        // TODO: This is done this way because LightGeometryNode enable but does not disable face culling.
        // TODO: When LightGeometryNode is switched to the new architecture, this will have to change.
        if (worldRenderer.isHeadUnderWater()) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }

        while (renderQueues.chunksAlphaBlend.size() > 0) {
            RenderableChunk chunk = renderQueues.chunksAlphaBlend.poll();

            if (chunk.hasMesh()) {
                final Vector3f chunkPosition = chunk.getPosition().toVector3f();
                final Vector3f chunkPositionRelativeToCamera =
                        new Vector3f(chunkPosition.x * ChunkConstants.SIZE_X - cameraPosition.x,
                                chunkPosition.y * ChunkConstants.SIZE_Y - cameraPosition.y,
                                chunkPosition.z * ChunkConstants.SIZE_Z - cameraPosition.z);

                chunkShader.setFloat3("chunkPositionWorld",
                        chunkPosition.x * ChunkConstants.SIZE_X,
                        chunkPosition.y * ChunkConstants.SIZE_Y,
                        chunkPosition.z * ChunkConstants.SIZE_Z,
                        true);
                chunkShader.setFloat("animated", chunk.isAnimated() ? 1.0f : 0.0f, true);

                // Effectively this just positions the chunk appropriately, relative to the camera.
                // chunkPositionRelativeToCamera = chunkCoordinates * chunkDimensions - cameraCoordinate
                GL11.glPushMatrix();
                GL11.glTranslatef(chunkPositionRelativeToCamera.x, chunkPositionRelativeToCamera.y, chunkPositionRelativeToCamera.z);

                chunk.getMesh().render(ChunkMesh.RenderPhase.REFRACTIVE);
                numberOfRenderedTriangles += chunk.getMesh().triangleCount();

                GL11.glPopMatrix(); // Resets the matrix stack after the rendering of a chunk.

            } else {
                numberOfChunksThatAreNotReadyYet++;
            }
        }

        chunkShader.deactivateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);

        if (worldRenderer.isHeadUnderWater()) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        }



        worldRenderer.increaseTrianglesCount(numberOfRenderedTriangles);
        worldRenderer.increaseNotReadyChunkCount(numberOfChunksThatAreNotReadyYet);

        PerformanceMonitor.endActivity();
    }

    /**
     * Sets the state for the rendering of the reflective/refractive features of the scene.
     * <p>
     * At this stage this is the surface of water bodies, reflecting the sky and (if enabled)
     * the surrounding landscape, and refracting the underwater scenery.
     * <p>
     * If the isHeadUnderWater argument is set to True, the state is further modified to
     * accommodate the rendering of the water surface from an underwater point of view.
     */

    /**
     * Resets the state after the rendering of the reflective/refractive features of the scene.
     * <p>
     * See preRenderSetupSceneReflectiveRefractive() for additional information.
     */

    @Override
    public void update() {
        // TODO: renames, maybe?
        READ_ONLY_GBUFFER.attachDepthBufferTo(displayResolutionDependentFBOs.get(REFRACTIVE_REFLECTIVE));
    }
}
