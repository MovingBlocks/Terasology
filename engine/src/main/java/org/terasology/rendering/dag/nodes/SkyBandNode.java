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
import org.terasology.registry.In;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;


/**
 * TODO: Diagram of this node
 */
public class SkyBandNode extends BlurNode {

    public static final ResourceUrn INTERMEDIATE_HAZE = new ResourceUrn("engine:intermediateHaze");
    public static final ResourceUrn FINAL_HAZE = new ResourceUrn("engine:finalHaze");

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    @In
    private Config config;

    private RenderingConfig renderingConfig;

    public void initialise(FBOConfig inputConfig, FBOConfig outputConfig, String aLabel) {

        float blurRadius = 8.0f;
        super.initialise(inputConfig, outputConfig, displayResolutionDependentFBOs, blurRadius, aLabel);

    }

    @Override
    protected void setupConditions() {
        renderingConfig = config.getRendering();
        renderingConfig.subscribe(RenderingConfig.INSCATTERING, this);
        requiresCondition(() -> renderingConfig.isInscattering());
    }
}
