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

import org.joml.Vector3f;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.math.TeraMath;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.OrthographicCamera;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableFaceCulling;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.ShadowMapResolutionDependentFBOs;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.RenderableWorld;
import org.terasology.world.chunks.RenderableChunk;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.terasology.rendering.primitives.ChunkMesh.RenderPhase.OPAQUE;

/**
 * This node class generates a shadow map used by the lighting step to determine what's in sight of
 * the main light (sun, moon) and what isn't, allowing the display of shadows cast from said light.
 * TODO: generalize to handle more than one light.
 *
 * Instances of this class:
 * - are enabled and disabled depending on the shadow setting in the rendering config.
 * - in VR mode regenerate the shadow map only once per frame rather than once per-eye.
 *
 * Diagram of this node can be viewed from:
 * TODO: move diagram to the wiki when this part of the code is stable
 * - https://docs.google.com/drawings/d/13I0GM9jDFlZv1vNrUPlQuBbaF86RPRNpVfn5q8Wj2lc/edit?usp=sharing
 */
public class ShadowMapNode extends ConditionDependentNode implements PropertyChangeListener {
    public static final SimpleUri SHADOW_MAP_FBO_URI = new SimpleUri("engine:fbo.sceneShadowMap");
    private static final ResourceUrn SHADOW_MAP_MATERIAL_URN = new ResourceUrn("engine:prog.shadowMap");
    private static final int SHADOW_FRUSTUM_BOUNDS = 500;
    private static final float STEP_SIZE = 50f;

    public Camera shadowMapCamera = new OrthographicCamera(-SHADOW_FRUSTUM_BOUNDS, SHADOW_FRUSTUM_BOUNDS, SHADOW_FRUSTUM_BOUNDS, -SHADOW_FRUSTUM_BOUNDS);

    private BackdropProvider backdropProvider;
    private RenderingConfig renderingConfig;
    private RenderQueuesHelper renderQueues;

    private SubmersibleCamera activeCamera;
    private float texelSize;

    public ShadowMapNode(String nodeUri, Context context) {
        super(nodeUri, context);

        renderQueues = context.get(RenderQueuesHelper.class);
        backdropProvider = context.get(BackdropProvider.class);
        renderingConfig = context.get(Config.class).getRendering();

        activeCamera = worldRenderer.getActiveCamera();

        context.get(RenderableWorld.class).setShadowMapCamera(shadowMapCamera);

        texelSize = 1.0f / renderingConfig.getShadowMapResolution() * 2.0f;
        renderingConfig.subscribe(RenderingConfig.SHADOW_MAP_RESOLUTION, this);

        requiresCondition(() -> renderingConfig.isDynamicShadows());
        renderingConfig.subscribe(RenderingConfig.DYNAMIC_SHADOWS, this);

        ShadowMapResolutionDependentFBOs shadowMapResolutionDependentFBOs = context.get(ShadowMapResolutionDependentFBOs.class);
        FBO shadowMapFbo = requiresFBO(new FBOConfig(SHADOW_MAP_FBO_URI, FBO.Type.NO_COLOR).useDepthBuffer(), shadowMapResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(shadowMapFbo));
        addDesiredStateChange(new SetViewportToSizeOf(shadowMapFbo));
        addDesiredStateChange(new EnableMaterial(SHADOW_MAP_MATERIAL_URN));

        addDesiredStateChange(new EnableFaceCulling());
    }

    private float calculateTexelSize(int shadowMapResolution) {
        return 1.0f / shadowMapResolution * 2.0f; // the 2.0 multiplier is currently a mystery.
    }

    /**
     * Handle changes to the following rendering config properties:
     *
     * - DYNAMIC_SHADOWS
     * - SHADOW_MAP_RESOLUTION
     *
     * It assumes the event gets fired only if one of the property has actually changed.
     *
     * @param event a PropertyChangeEvent instance, carrying information regarding
     *              what property changed, its old value and its new value.
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();

        switch (propertyName) {
            case RenderingConfig.DYNAMIC_SHADOWS:
                super.propertyChange(event);
                break;

            case RenderingConfig.SHADOW_MAP_RESOLUTION:
                int shadowMapResolution = (int) event.getNewValue();
                texelSize = calculateTexelSize(shadowMapResolution);
                break;

            // default: no other cases are possible - see subscribe operations in initialize().
        }
    }

    /**
     * Re-positions the shadow map camera to loosely match the position of the main light (sun, moon), then
     * writes depth information from that camera into a depth buffer, to be used later to create shadows.
     *
     * The loose match is to avoid flickering: the shadowmap only moves in steps while the main light actually
     * moves continuously.
     *
     * This method is executed within a NodeTask in the Render Tasklist, but its calculations are executed
     * only once per frame. I.e. in VR mode they are executed only when the left eye is processed. This is
     * done in the assumption that we do not need to generate and use a shadow map for each eye as it wouldn't
     * be noticeable.
     */
    @Override
    public void process() {
        // TODO: remove this IF statement when VR is handled via parallel nodes, one per eye.
        if (worldRenderer.isFirstRenderingStageForCurrentFrame()) {
            PerformanceMonitor.startActivity("rendering/" + getUri());

            // Actual Node Processing

            positionShadowMapCamera(); // TODO: extract these calculation into a separate node.

            int numberOfRenderedTriangles = 0;
            int numberOfChunksThatAreNotReadyYet = 0;

            final Vector3f cameraPosition = shadowMapCamera.getPosition();

            shadowMapCamera.lookThrough();

            // FIXME: storing chunksOpaqueShadow or a mechanism for requesting a chunk queue for nodes which calls renderChunks method?
            while (renderQueues.chunksOpaqueShadow.size() > 0) {
                RenderableChunk chunk = renderQueues.chunksOpaqueShadow.poll();

                if (chunk.hasMesh()) {
                    final ChunkMesh chunkMesh = chunk.getMesh();
                    final Vector3f chunkPosition = new Vector3f(chunk.getPosition());

                    numberOfRenderedTriangles += chunkMesh.render(OPAQUE, chunkPosition, cameraPosition);

                } else {
                    numberOfChunksThatAreNotReadyYet++;
                }
            }

            worldRenderer.increaseTrianglesCount(numberOfRenderedTriangles);
            worldRenderer.increaseNotReadyChunkCount(numberOfChunksThatAreNotReadyYet);

            PerformanceMonitor.endActivity();
        }
    }

    private void positionShadowMapCamera() {
        // We begin by setting our light coordinates at the player coordinates, ignoring the player's altitude
        Vector3f mainLightPosition = new Vector3f(activeCamera.getPosition().x, 0.0f, activeCamera.getPosition().z); // world-space coordinates

        // The shadow projected onto the ground must move in in light-space texel-steps, to avoid causing flickering.
        // That's why we first convert it to the previous frame's light-space coordinates and then back to world-space.
        shadowMapCamera.getViewProjectionMatrix().transformPosition(mainLightPosition); // to light-space
        mainLightPosition.set(TeraMath.fastFloor(mainLightPosition.x / texelSize) * texelSize, 0.0f,
                              TeraMath.fastFloor(mainLightPosition.z / texelSize) * texelSize);
        shadowMapCamera.getInverseViewProjectionMatrix().transformPosition(mainLightPosition); // back to world-space

        // This is what causes the shadow map to change infrequently, to prevent flickering.
        // Notice that this is different from what is done above, which is about spatial steps
        // and is related to the player's position and texels.
        Vector3f quantizedMainLightDirection = getQuantizedMainLightDirection(STEP_SIZE);

        // The shadow map camera is placed away from the player, in the direction of the main light.
        Vector3f offsetFromPlayer = new Vector3f(quantizedMainLightDirection);
        offsetFromPlayer.mul(256.0f + 64.0f); // these hardcoded numbers are another mystery.
        mainLightPosition.add(offsetFromPlayer);
        shadowMapCamera.getPosition().set(mainLightPosition);

        // Finally, we adjust the shadow map camera to look toward the player
        Vector3f fromLightToPlayerDirection = new Vector3f(quantizedMainLightDirection);
        fromLightToPlayerDirection.mul(-1.0f);
        shadowMapCamera.getViewingDirection().set(fromLightToPlayerDirection);

        shadowMapCamera.update(worldRenderer.getSecondsSinceLastFrame());
    }

    private Vector3f getQuantizedMainLightDirection(float stepSize) {
        float mainLightAngle = (float) Math.floor((double) backdropProvider.getSunPositionAngle() * stepSize) / stepSize + 0.0001f;
        Vector3f mainLightDirection = new Vector3f(0.0f, (float) Math.cos(mainLightAngle), (float) Math.sin(mainLightAngle));

        // When the sun goes under the horizon we flip the vector, to provide the moon direction, and viceversa.
        if (mainLightDirection.y < 0.0f) {
            mainLightDirection.mul(-1.0f);
        }

        return mainLightDirection;
    }
}
