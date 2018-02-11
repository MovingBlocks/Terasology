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
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * This class is a thin facade in front of the BlurNode class it inherits from.
 * The term "late" is due to the fact that this type of nodes is used near the
 * end of the rendering process leading to an image shown on the user display.
 *
 * Given an input FBO a blurred version of it will be stored in the given output FBO.
 * Eventually the blurred version can be used for blur-based effects such as
 * Depth of Field.
 *
 * For more information on Blur: https://en.wikipedia.org/wiki/Box_blur
 * For more information on DoF: http://en.wikipedia.org/wiki/Depth_of_field
 */
public class LateBlurNode extends BlurNode implements PropertyChangeListener {
    public static final SimpleUri FIRST_LATE_BLUR_FBO_URI = new SimpleUri("engine:fbo.firstLateBlur");
    public static final SimpleUri SECOND_LATE_BLUR_FBO_URI = new SimpleUri("engine:fbo.secondLateBlur");

    @Range(min = 0.0f, max = 16.0f)
    private static final float OVERALL_BLUR_RADIUS_FACTOR = 0.8f;

    private RenderingConfig renderingConfig;

    /**
     * Constructs a LateBlurNode instance.
     *
     * @param inputFbo The input fbo, containing the image to be blurred.
     * @param outputFbo The output fbo, to store the blurred image.
     */
    public LateBlurNode(Context context, FBO inputFbo, FBO outputFbo) {
        super(context, inputFbo, outputFbo, 0); // note: blurRadius is 0.0 at this stage.
        updateBlurRadius(); // only here blurRadius is properly set.
    }

    /**
     * This method establishes the conditions in which the blur will take place, by enabling or disabling the node.
     *
     * In this particular case the node is enabled if RenderingConfig.getBlurIntensity is not 0 - or blur is enabled.
     */
    @Override
    protected void setupConditions(Context context) {
        renderingConfig = context.get(Config.class).getRendering();
        renderingConfig.subscribe(RenderingConfig.BLUR_INTENSITY, this);
        requiresCondition(() -> renderingConfig.getBlurIntensity() != 0);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        updateBlurRadius();
    }

    private void updateBlurRadius() {
        this.blurRadius = OVERALL_BLUR_RADIUS_FACTOR * Math.max(1, renderingConfig.getBlurIntensity());
    }
}
