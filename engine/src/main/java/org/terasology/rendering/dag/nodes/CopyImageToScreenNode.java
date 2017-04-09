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

import org.lwjgl.opengl.Display;
import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.monitoring.PerformanceMonitor;

import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.opengl.GL11.glViewport;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.FINAL_BUFFER;
import static org.terasology.rendering.world.WorldRenderer.RenderingStage.LEFT_EYE;
import static org.terasology.rendering.world.WorldRenderer.RenderingStage.MONO;

public class CopyImageToScreenNode extends ConditionDependentNode implements FBOManagerSubscriber {
    private static final ResourceUrn DEFAULT_FBO = new ResourceUrn("engine:display");
    private static final ResourceUrn DEFAULT_TEXTURED_MATERIAL = new ResourceUrn("engine:prog.defaultTextured");

    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private FBO sceneFinalFbo;
    private int displayWidth;
    private int displayHeight;

    public CopyImageToScreenNode(Context context) {
        WorldRenderer worldRenderer = context.get(WorldRenderer.class);
        displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);

        requiresCondition(() -> worldRenderer.getCurrentRenderStage() == MONO || worldRenderer.getCurrentRenderStage() == LEFT_EYE);
        addDesiredStateChange(new BindFBO(DEFAULT_FBO, displayResolutionDependentFBOs));
        update(); // Cheeky way to initialise sceneFinalFbo
        displayResolutionDependentFBOs.subscribe(this);

        addDesiredStateChange(new EnableMaterial(DEFAULT_TEXTURED_MATERIAL));
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/copyImageToScreen");
        sceneFinalFbo.bindTexture(); // TODO: Convert to a StateChange
        // The way things are set-up right now, we can have FBOs that are not the same size as the display (if scale != 100%).
        // However, when drawing the final image to the screen, we always want the viewport to match the size of display,
        // and not that of some FBO. Hence, we are manually setting the viewport via glViewport over here.
        glViewport(0, 0, displayWidth, displayHeight);
        renderFullscreenQuad();
        PerformanceMonitor.endActivity();
    }

    @Override
    public void update() {
        sceneFinalFbo = displayResolutionDependentFBOs.get(FINAL_BUFFER);
        displayWidth = Display.getWidth();
        displayHeight = Display.getHeight();
    }
}
