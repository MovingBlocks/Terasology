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
import org.terasology.engine.SimpleUri;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.DepthStencilTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

/**
 * This nodes (or rather the shader used by it) takes advantage of the Sobel operator [1]
 * to trace outlines (silhouette edges) of objects at some distance from the player.
 *
 * The resulting outlines are stored in a separate buffer the content of which is
 * later composed over the more complete rendering of the 3d scene.
 *
 * [1] https://en.wikipedia.org/wiki/Sobel_operator
 */
public class OutlineNode extends ConditionDependentNode {
    public static final SimpleUri OUTLINE_FBO_URI = new SimpleUri("engine:fbo.outline");
    private static final ResourceUrn OUTLINE_MATERIAL_URN = new ResourceUrn("engine:prog.sobel");

    private RenderingConfig renderingConfig;
    private SubmersibleCamera activeCamera;

    private Material outlineMaterial;

    private FBO lastUpdatedGBuffer;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 16.0f)
    private float pixelOffsetX = 1.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 16.0f)
    private float pixelOffsetY = 1.0f;

    public OutlineNode(String nodeUri, Context context) {
        super(nodeUri, context);

        activeCamera = worldRenderer.getActiveCamera();

        renderingConfig = context.get(Config.class).getRendering();
        renderingConfig.subscribe(RenderingConfig.OUTLINE, this);
        requiresCondition(() -> renderingConfig.isOutline());

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        lastUpdatedGBuffer = displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo();
        FBO outlineFbo = requiresFBO(new FBOConfig(OUTLINE_FBO_URI, FULL_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(outlineFbo));

        addDesiredStateChange(new EnableMaterial(OUTLINE_MATERIAL_URN));

        outlineMaterial = getMaterial(OUTLINE_MATERIAL_URN);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot, lastUpdatedGBuffer, DepthStencilTexture, displayResolutionDependentFBOs, OUTLINE_MATERIAL_URN, "texDepth"));
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
        PerformanceMonitor.startActivity("rendering/" + getUri());

        // Shader Parameters

        outlineMaterial.setFloat3("cameraParameters", activeCamera.getzNear(), activeCamera.getzFar(), 0.0f, true);

        outlineMaterial.setFloat("texelWidth", 1.0f / lastUpdatedGBuffer.width());
        outlineMaterial.setFloat("texelHeight", 1.0f / lastUpdatedGBuffer.height());

        outlineMaterial.setFloat("pixelOffsetX", pixelOffsetX);
        outlineMaterial.setFloat("pixelOffsetY", pixelOffsetY);

        // Actual Node Processing

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}
