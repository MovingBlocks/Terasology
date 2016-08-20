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
import org.terasology.rendering.dag.stateChanges.SetViewportSizeOf;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import static org.terasology.rendering.opengl.ScalingFactors.HALF_SCALE;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO: Add diagram of this node
 */
public class LightShaftsNode extends ConditionDependentNode {
    public static final ResourceUrn LIGHT_SHAFTS = new ResourceUrn("engine:lightShafts");

    @In
    private Config config;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    @In
    private WorldRenderer worldRenderer;

    private RenderingConfig renderingConfig;

    @Override
    public void initialise() {
        renderingConfig = config.getRendering();

        renderingConfig.subscribe(renderingConfig.LIGHT_SHAFTS, this);
        requiresCondition(() -> renderingConfig.isLightShafts());
        requiresFBO(new FBOConfig(LIGHT_SHAFTS, HALF_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        addDesiredStateChange(new EnableMaterial("engine:prog.lightshaft")); // TODO: rename shader to lightShafts
        addDesiredStateChange(new BindFBO(LIGHT_SHAFTS, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportSizeOf(LIGHT_SHAFTS, displayResolutionDependentFBOs));
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/lightShafts");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary
        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}
