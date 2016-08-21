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
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

/**
 * TODO: Add diagram of this node
 */
public class OutlineNode extends ConditionDependentNode {
    public static final ResourceUrn OUTLINE = new ResourceUrn("engine:outline");

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    private RenderingConfig renderingConfig;

    @Override
    public void initialise() {
        renderingConfig = config.getRendering();
        renderingConfig.subscribe(renderingConfig.OUTLINE, this);
        requiresCondition(() -> renderingConfig.isOutline());
        requiresFBO(new FBOConfig(OUTLINE, FULL_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        addDesiredStateChange(new EnableMaterial("engine:prog.sobel"));
        // TODO: verify inputs: shouldn't there be a texture binding here?
        addDesiredStateChange(new BindFBO(OUTLINE, displayResolutionDependentFBOs));
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

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary
        renderFullscreenQuad();

        PerformanceMonitor.endActivity();

    }
}
