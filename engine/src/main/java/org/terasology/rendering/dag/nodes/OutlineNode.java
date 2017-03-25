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
        renderingConfig.subscribe(RenderingConfig.OUTLINE, this);
        requiresCondition(() -> renderingConfig.isOutline());

        requiresFBO(new FBOConfig(OUTLINE, FULL_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(OUTLINE, displayResolutionDependentFBOs));

        addDesiredStateChange(new EnableMaterial("engine:prog.sobel"));

        // TODO: Here make Material-based texture bindings explicit, using StateChanges.
        // TODO: See for example the ApplyDeferredLightingNode as an example of setting input textures
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

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();

    }
}
