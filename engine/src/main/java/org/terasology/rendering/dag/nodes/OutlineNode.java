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
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.DepthStencilTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;

/**
 * This nodes (or rather the shader used by it) takes advantage of the Sobel operator [1]
 * to trace outlines (silhouette edges) of objects at some distance from the player.
 *
 * The resulting outlines are stored in a separate buffer the content of which is
 * later composed over the more complete rendering of the 3d scene.
 *
 * [1] https://en.wikipedia.org/wiki/Sobel_operator
 */
public class OutlineNode extends ConditionDependentNode implements FBOManagerSubscriber {
    public static final ResourceUrn OUTLINE_FBO = new ResourceUrn("engine:outline");
    public static final ResourceUrn OUTLINE_MATERIAL = new ResourceUrn("engine:prog.sobel");

    private BackdropProvider backdropProvider;
    private WorldRenderer worldRenderer;
    private RenderingConfig renderingConfig;
    private WorldProvider worldProvider;
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private Material outlineMaterial;

    @SuppressWarnings("FieldCanBeLocal")
    private FBO sceneOpaqueFbo;
    private float sceneOpaqueFboWidth;
    private float sceneOpaqueFboHeight;

    @Range(min = 0.0f, max = 16.0f)
    float pixelOffsetX = 1.0f;
    @Range(min = 0.0f, max = 16.0f)
    float pixelOffsetY = 1.0f;

    private SubmersibleCamera activeCamera;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f sunDirection;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f cameraDir;
    @SuppressWarnings("FieldCanBeLocal")
    private Vector3f cameraPosition;

    public OutlineNode(Context context) {
        super(context);

        backdropProvider = context.get(BackdropProvider.class);
        worldProvider = context.get(WorldProvider.class);
        worldRenderer = context.get(WorldRenderer.class);

        activeCamera = worldRenderer.getActiveCamera();

        renderingConfig = context.get(Config.class).getRendering();
        renderingConfig.subscribe(RenderingConfig.OUTLINE, this);
        requiresCondition(() -> renderingConfig.isOutline());

        displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        requiresFBO(new FBOConfig(OUTLINE_FBO, FULL_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(OUTLINE_FBO, displayResolutionDependentFBOs));

        update(); // Cheeky way to initialise sceneOpaqueFboWidth, sceneOpaqueFboHeight
        displayResolutionDependentFBOs.subscribe(this);

        addDesiredStateChange(new EnableMaterial(OUTLINE_MATERIAL));

        outlineMaterial = getMaterial(OUTLINE_MATERIAL);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot, READONLY_GBUFFER, DepthStencilTexture, displayResolutionDependentFBOs, OUTLINE_MATERIAL, "texDepth"));
    }

    /**
     * Enabled by the "outline" option in the render settings, this method generates
     * landscape/objects outlines and stores them into a buffer in its own FBO. The
     * stored image is eventually combined with others.
     * <p>
     * The outlines visually separate a given object (including the landscape) or parts of it
     * from sufficiently distant objects it overlaps. It is effectively a depth-based edge
     * detection technique and internally uses a Sobel operator.
     * <p>
     * For further information see: http://en.wikipedia.org/wiki/Sobel_operator
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/outline");

        // Common Shader Parameters

        outlineMaterial.setFloat("viewingDistance", renderingConfig.getViewDistance().getChunkDistance().x * 8.0f, true);

        outlineMaterial.setFloat("daylight", backdropProvider.getDaylight(), true);
        outlineMaterial.setFloat("tick", worldRenderer.getMillisecondsSinceRenderingStart(), true);
        outlineMaterial.setFloat("sunlightValueAtPlayerPos", worldRenderer.getTimeSmoothedMainLightIntensity(), true);

        cameraDir = activeCamera.getViewingDirection();
        cameraPosition = activeCamera.getPosition();

        outlineMaterial.setFloat("swimming", activeCamera.isUnderWater() ? 1.0f : 0.0f, true);
        outlineMaterial.setFloat3("cameraPosition", cameraPosition.x, cameraPosition.y, cameraPosition.z, true);
        outlineMaterial.setFloat3("cameraDirection", cameraDir.x, cameraDir.y, cameraDir.z, true);
        outlineMaterial.setFloat3("cameraParameters", activeCamera.getzNear(), activeCamera.getzFar(), 0.0f, true);

        sunDirection = backdropProvider.getSunDirection(false);
        outlineMaterial.setFloat3("sunVec", sunDirection.x, sunDirection.y, sunDirection.z, true);

        outlineMaterial.setFloat("time", worldProvider.getTime().getDays(), true);

        // Specific Shader Parameters

        outlineMaterial.setFloat("texelWidth", 1.0f / sceneOpaqueFboWidth);
        outlineMaterial.setFloat("texelHeight", 1.0f / sceneOpaqueFboHeight);

        outlineMaterial.setFloat("pixelOffsetX", pixelOffsetX);
        outlineMaterial.setFloat("pixelOffsetY", pixelOffsetY);

        // Actual Node Processing

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();

    }

    @Override
    public void update() {
        sceneOpaqueFbo = displayResolutionDependentFBOs.get(READONLY_GBUFFER);
        sceneOpaqueFboWidth = sceneOpaqueFbo.width();
        sceneOpaqueFboHeight = sceneOpaqueFbo.height();
    }
}
