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
import org.terasology.rendering.opengl.FBO;

/**
 * If bloom is enabled via the rendering settings, this method generates the blurred images needed
 * for the bloom shader effect and stores them in their own frame buffers.
 * <p>
 * This effects renders adds fringes (or "feathers") of light to areas of intense brightness.
 * This in turn give the impression of those areas partially overwhelming the camera or the eye.
 * <p>
 * For more information see: http://en.wikipedia.org/wiki/Bloom_(shader_effect)
 */
public class BloomBlurNode extends BlurNode {
    public static final SimpleUri HALF_SCALE_FBO_URI = new SimpleUri("engine:fbo.halfScaleBlurredBloom");
    public static final SimpleUri QUARTER_SCALE_FBO_URI = new SimpleUri("engine:fbo.quarterScaleBlurredBloom");
    public static final SimpleUri ONE_8TH_SCALE_FBO_URI = new SimpleUri("engine:fbo.one8thScaleBlurredBloom");
    private static final float BLUR_RADIUS = 12.0f;

    /**
     * Constructs a BloomBlurNode instance. This method must be called once shortly after instantiation
     * to fully initialize the node and make it ready for rendering.
     *
     * @param inputFbo The input fbo, containing the image to be blurred.
     * @param outputFbo The output fbo, to store the blurred image.
     */
    public BloomBlurNode(String nodeUri, Context context, FBO inputFbo, FBO outputFbo) {
        super(nodeUri, context, inputFbo, outputFbo, BLUR_RADIUS);

        RenderingConfig renderingConfig = context.get(Config.class).getRendering();
        requiresCondition(renderingConfig::isBloom);
        renderingConfig.subscribe(RenderingConfig.BLOOM, this);
    }
}
