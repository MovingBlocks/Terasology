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
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportSizeOf;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO: Add diagram of this node
 */
public class ToneMappingNode extends AbstractNode {
    public static final ResourceUrn TONE_MAPPED = new ResourceUrn("engine:sceneToneMapped"); // HDR tone mapping

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    @Override
    public void initialise() {
        requiresFBO(new FBOConfig(TONE_MAPPED, FULL_SCALE, FBO.Type.HDR), displayResolutionDependentFBOs);

        addDesiredStateChange(new BindFBO(TONE_MAPPED, displayResolutionDependentFBOs));
        addDesiredStateChange(new EnableMaterial("engine:prog.hdr")); // TODO: rename shader to toneMapping)
        addDesiredStateChange(new SetViewportSizeOf(TONE_MAPPED, displayResolutionDependentFBOs));
    }

    /**
     * // TODO: write javadoc
     */
    // TODO: Tone mapping usually maps colors from HDR to a more limited range,
    // TODO: i.e. the 24 bit a monitor can display. This method however maps from an HDR buffer
    // TODO: to another HDR buffer and this puzzles me. Will need to dig deep in the shader to
    // TODO: see what it does.
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/toneMapping");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);     // TODO: verify this is necessary
        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}
