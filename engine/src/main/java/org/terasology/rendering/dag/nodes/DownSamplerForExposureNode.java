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

import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;

/**
 * Extends the DownSamplerNode class adding setup conditions and fbo configs needed to calculate the exposure value.
 *
 * Specifically:
 * A) it override the setupConditions() method so that instances of this class are enabled if isEyeAdaptation() returns true
 * B) it provide a number of FBOConfigs used to downsample the rendering multiple times, down to 1x1 pixels
 *
 * Once the rendering achieved so far has been downsampled to a 1x1 pixel image the RGB values of the pixel effectively
 * encode the average brightness of the rendering, which in turn is used to tweak the exposure parameter later nodes use.
 */
public class DownSamplerForExposureNode extends DownSamplerNode {
    public static final FBOConfig FBO_16X16_CONFIG = new FBOConfig(new SimpleUri("engine:fbo.16x16px"), 16, 16, FBO.Type.DEFAULT);
    public static final FBOConfig FBO_8X8_CONFIG = new FBOConfig(new SimpleUri("engine:fbo.8x8px"), 8, 8, FBO.Type.DEFAULT);
    public static final FBOConfig FBO_4X4_CONFIG = new FBOConfig(new SimpleUri("engine:fbo.4x4px"), 4, 4, FBO.Type.DEFAULT);
    public static final FBOConfig FBO_2X2_CONFIG = new FBOConfig(new SimpleUri("engine:fbo.2x2px"), 2, 2, FBO.Type.DEFAULT);
    public static final FBOConfig FBO_1X1_CONFIG = new FBOConfig(new SimpleUri("engine:fbo.1x1px"), 1, 1, FBO.Type.DEFAULT);

    public DownSamplerForExposureNode(String nodeUri, Context context,
                                                        FBOConfig inputFboConfig, BaseFBOsManager inputFboManager,
                                                        FBOConfig outputFboConfig, BaseFBOsManager outputFboManager) {
        super(nodeUri, context, inputFboConfig, inputFboManager, outputFboConfig, outputFboManager);

        RenderingConfig renderingConfig = context.get(Config.class).getRendering();
        requiresCondition(renderingConfig::isEyeAdaptation);

        renderingConfig.subscribe(RenderingConfig.EYE_ADAPTATION, this);
    }
}
