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
import org.terasology.config.RenderingConfig;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.WireframeCapableNode;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_16TH_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_32TH_SCALE;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.bindDisplay;
import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO: Diagram of this node
 * TODO: Separate this node into multiple SkyBandNode's
 */
public class SkyBandsNode extends WireframeCapableNode {
    public static final ResourceUrn SKY_BAND_0 = new ResourceUrn("engine:sceneSkyBand0");
    public static final ResourceUrn SKY_BAND_1 = new ResourceUrn("engine:sceneSkyBand1");

    @In
    private WorldRenderer worldRenderer;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private RenderingConfig renderingConfig;
    private Material blurShader;

    private FBO sceneSkyBand0;
    private FBO sceneSkyBand1;
    private Camera playerCamera;


    @Override
    public void initialise() {
        super.initialise();

        requiresFBO(new FBOConfig(SKY_BAND_0, ONE_16TH_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        requiresFBO(new FBOConfig(SKY_BAND_1, ONE_32TH_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);

        renderingConfig = config.getRendering();
        blurShader = worldRenderer.getMaterial("engine:prog.blur");
        playerCamera = worldRenderer.getActiveCamera();
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/skyBands");

        READ_ONLY_GBUFFER.setRenderBufferMask(true, true, true);
        if (renderingConfig.isInscattering()) {
            sceneSkyBand0 = displayResolutionDependentFBOs.get(SKY_BAND_0);
            sceneSkyBand1 = displayResolutionDependentFBOs.get(SKY_BAND_1);

            generateSkyBand(sceneSkyBand0);
            generateSkyBand(sceneSkyBand1);
        }

        READ_ONLY_GBUFFER.bind();

        playerCamera.lookThrough();

        PerformanceMonitor.endActivity();
    }

    private void generateSkyBand(FBO skyBand) {
        blurShader.enable();
        blurShader.setFloat("radius", 8.0f, true);
        blurShader.setFloat2("texelSize", 1.0f / skyBand.width(), 1.0f / skyBand.height(), true);

        if (skyBand == sceneSkyBand0) {
            READ_ONLY_GBUFFER.bindTexture();
        } else {
            sceneSkyBand0.bindTexture();
        }

        skyBand.bind();
        skyBand.setRenderBufferMask(true, false, false);

        setViewportToSizeOf(skyBand);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        bindDisplay();     // TODO: verify this is necessary
        setViewportToSizeOf(READ_ONLY_GBUFFER); // TODO: verify this is necessary
    }
}
