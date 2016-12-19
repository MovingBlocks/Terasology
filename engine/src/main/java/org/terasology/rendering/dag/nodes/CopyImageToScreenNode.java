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
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.config.Config;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;

import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.FINAL;
import static org.terasology.rendering.world.WorldRenderer.RenderingStage.LEFT_EYE;
import static org.terasology.rendering.world.WorldRenderer.RenderingStage.MONO;

public class CopyImageToScreenNode extends ConditionDependentNode {

    private static final ResourceUrn DEFAULT_FRAME_BUFFER_URN = new ResourceUrn("engine:display");

    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;


    @Override
    public void initialise() {
        requiresCondition(() -> worldRenderer.getCurrentRenderStage() == MONO || worldRenderer.getCurrentRenderStage() == LEFT_EYE);
        addDesiredStateChange(new BindFBO(DEFAULT_FRAME_BUFFER_URN, displayResolutionDependentFBOs));
        addDesiredStateChange(new EnableMaterial("engine:prog.defaultTextured"));
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/copyImageToScreen");
        FINAL.bindTexture();
        renderFullscreenQuad();
        PerformanceMonitor.endActivity();
    }
}
