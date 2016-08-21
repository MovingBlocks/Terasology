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

import org.lwjgl.opengl.GL11;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import org.terasology.rendering.dag.stateChanges.DisableDepthMask;
import org.terasology.rendering.dag.stateChanges.EnableBlending;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

/**
 * TODO: Add diagram of this node
 */
public class SimpleBlendMaterialsNode extends AbstractNode {

    @In
    private ComponentSystemManager componentSystemManager;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    @Override
    public void initialise() {
        addDesiredStateChange(new BindFBO(READ_ONLY_GBUFFER.getName(), displayResolutionDependentFBOs)); // TODO: might be removed, verify it
        // TODO: review - might be redundant to setRenderBufferMask(sceneOpaque) again at the end of the process() method

        /**
         * Sets the state for the rendering of objects or portions of objects having some degree of transparency.
         * <p>
         * Generally speaking objects drawn with this state will have their color blended with the background
         * color, depending on their opacity. I.e. a 25% opaque foreground object will provide 25% of its
         * color while the background will provide the remaining 75%. The sum of the two RGBA vectors gets
         * written onto the output buffer.
         * <p>
         * Important note: this method disables writing to the Depth Buffer. This is why filters relying on
         * depth information (i.e. DoF) have problems with transparent objects: the depth of their pixels is
         * found to be that of the background. This is an unresolved (unresolv-able?) issue that would only
         * be reversed, not eliminated, by re-enabling writing to the Depth Buffer.
         */
        addDesiredStateChange(new EnableBlending());
        addDesiredStateChange(new DisableDepthMask());
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/simpleBlendMaterials");
        READ_ONLY_GBUFFER.setRenderBufferMask(true, true, true);
        // TODO: make a glBlendFunc StateChange and eliminate this line
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); // (*)

        // (*) In this context SRC is Foreground. This effectively says:
        // Resulting RGB = ForegroundRGB * ForegroundAlpha + BackgroundRGB * (1 - ForegroundAlpha)
        // Which might still look complicated, but it's actually the most typical alpha-driven composite.
        // A neat tool to play with this settings can be found here: http://www.andersriggelsen.dk/glblendfunc.php

        for (RenderSystem renderer : componentSystemManager.iterateRenderSubscribers()) {
            renderer.renderAlphaBlend();
        }

        PerformanceMonitor.endActivity();
    }
}
