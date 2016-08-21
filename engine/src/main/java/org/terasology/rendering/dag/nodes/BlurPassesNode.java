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
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.nui.properties.Range;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.HALF_SCALE;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.bindDisplay;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;

/**
 * TODO: Add diagram of this node
 */
public class BlurPassesNode extends ConditionDependentNode {
    public static final ResourceUrn BLUR_0 = new ResourceUrn("engine:sceneBlur0");
    public static final ResourceUrn BLUR_1 = new ResourceUrn("engine:sceneBlur1");
    public static final ResourceUrn TONE_MAPPED = new ResourceUrn("engine:sceneToneMapped");

    @Range(min = 0.0f, max = 16.0f)
    private float overallBlurRadiusFactor = 0.8f;

    @In
    private Config config;

    @In
    private WorldRenderer worldRenderer;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private RenderingConfig renderingConfig;
    private Material blur;
    private FBO sceneBlur0;
    private FBO sceneBlur1;

    private FBO sceneToneMapped;

    @Override
    public void initialise() {
        renderingConfig = config.getRendering();
        blur = worldRenderer.getMaterial("engine:prog.blur");

        renderingConfig.subscribe(RenderingConfig.BLUR_INTENSITY, this);
        requiresCondition(() -> renderingConfig.getBlurIntensity() != 0);
        requiresFBO(new FBOConfig(BLUR_0, HALF_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        requiresFBO(new FBOConfig(BLUR_1, HALF_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        requiresFBO(new FBOConfig(TONE_MAPPED, FULL_SCALE, FBO.Type.HDR), displayResolutionDependentFBOs);
    }

    /**
     * If blur is enabled through the rendering settings, this method generates the images used
     * by the Blur effect when underwater and for the Depth of Field effect when above water.
     * <p>
     * For more information on blur: http://en.wikipedia.org/wiki/Defocus_aberration
     * For more information on DoF: http://en.wikipedia.org/wiki/Depth_of_field
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/blurPasses");

        sceneBlur0 = displayResolutionDependentFBOs.get(BLUR_0);
        sceneBlur1 = displayResolutionDependentFBOs.get(BLUR_1);
        sceneToneMapped = displayResolutionDependentFBOs.get(TONE_MAPPED);

        generateBlur(sceneBlur0);
        generateBlur(sceneBlur1);
        PerformanceMonitor.endActivity();
    }

    private void generateBlur(FBO sceneBlur) {
        blur.enable();
        blur.setFloat("radius", overallBlurRadiusFactor * renderingConfig.getBlurRadius(), true);
        blur.setFloat2("texelSize", 1.0f / sceneBlur.width(), 1.0f / sceneBlur.height(), true);

        if (sceneBlur == sceneBlur0) {
            sceneToneMapped.bindTexture();
        } else {
            sceneBlur0.bindTexture();
        }

        sceneBlur.bind();

        setViewportToSizeOf(sceneBlur);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        bindDisplay(); // TODO: verify this is necessary
        setViewportToSizeOf(READ_ONLY_GBUFFER); // TODO: verify this is necessary
    }
}
