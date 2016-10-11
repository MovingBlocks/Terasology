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
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;

import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_16TH_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_32TH_SCALE;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.utilities.Assets;

import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO: Diagram of this node
 * TODO: Separate this node into multiple SkyBandNode's
 */
public class SkyBandsNode extends ConditionDependentNode {
    public static final ResourceUrn SKY_BAND_0 = new ResourceUrn("engine:sceneSkyBand0");
    public static final ResourceUrn SKY_BAND_1 = new ResourceUrn("engine:sceneSkyBand1");

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    @In
    private Config config;

    private RenderingConfig renderingConfig;
    private Material blurShader;

    private FBO sceneSkyBand0;
    private FBO sceneSkyBand1;


    @Override
    public void initialise() {

        requiresFBO(new FBOConfig(SKY_BAND_0, ONE_16TH_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        requiresFBO(new FBOConfig(SKY_BAND_1, ONE_32TH_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);

        renderingConfig = config.getRendering();
        renderingConfig.subscribe(RenderingConfig.INSCATTERING, this);
        requiresCondition(() -> renderingConfig.isInscattering());

        addDesiredStateChange(new EnableMaterial("engine:prog.blur"));
        blurShader = Assets.getMaterial("engine:prog.blur").orElseThrow(() ->
                new RuntimeException("Failed to resolve required asset: '" + "engine:prog.blur" + "'"));
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/skyBands");

        blurShader.setFloat("radius", 8.0f, true);

        sceneSkyBand0 = displayResolutionDependentFBOs.get(SKY_BAND_0);
        blurShader.setFloat2("texelSize", 1.0f / sceneSkyBand0.width(), 1.0f / sceneSkyBand0.height(), true);
        READ_ONLY_GBUFFER.bindTexture();
        sceneSkyBand0.bind();
        setViewportToSizeOf(sceneSkyBand0);
        renderFullscreenQuad();

        sceneSkyBand1 = displayResolutionDependentFBOs.get(SKY_BAND_1);
        blurShader.setFloat2("texelSize", 1.0f / sceneSkyBand1.width(), 1.0f / sceneSkyBand1.height(), true);
        sceneSkyBand0.bindTexture();
        sceneSkyBand1.bind();
        setViewportToSizeOf(sceneSkyBand1);
        renderFullscreenQuad();

        READ_ONLY_GBUFFER.bind();
        setViewportToSizeOf(READ_ONLY_GBUFFER); // TODO: verify this is necessary

        PerformanceMonitor.endActivity();
    }

}
