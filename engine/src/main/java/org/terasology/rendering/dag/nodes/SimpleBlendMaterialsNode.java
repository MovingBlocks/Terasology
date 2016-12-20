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

import org.terasology.engine.ComponentSystemManager;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import org.terasology.rendering.dag.stateChanges.DisableDepthWriting;
import org.terasology.rendering.dag.stateChanges.EnableBlending;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;

import org.terasology.rendering.dag.stateChanges.SetBlendFunction;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

/**
 * An instance of this class renders and blends semi-transparent objects into the content of the existing g-buffer.
 *
 * Notice that this is handled in the process() method by calling the renderAlphaBlend() method of registered
 * instances implementing the RenderSystem interface.
 *
 * Theoretically the same results could be achieved by rendering all meshes in one go, keeping blending
 * always enabled and relying on the alpha channel of the textures associated with a given mesh. In practice
 * blending is an expensive operation and it wouldn't be good performance-wise to keep it always enabled.
 *
 * Also, a number of previous nodes rely on unambiguous meaning for the depth values in the gbuffers,
 * but this node temporarily disable writing to the depth buffer - what value should be written to it,
 * the distance to the semi-transparent surface or what's already stored in the depth buffer? As such
 * semi-transparent objects are handled here, after nodes relying on the depth buffer have done their job.
 */
public class SimpleBlendMaterialsNode extends AbstractNode {

    @In
    private ComponentSystemManager componentSystemManager;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    /**
     * This method must be called once shortly after instantiation to fully initialize the node
     * and make it ready for rendering.
     */
    @Override
    public void initialise() {
        addDesiredStateChange(new BindFBO(READ_ONLY_GBUFFER.getName(), displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(READ_ONLY_GBUFFER.getName(), displayResolutionDependentFBOs));

        // Sets the state for the rendering of objects or portions of objects having some degree of transparency.
        // Generally speaking objects drawn with this state will have their color blended with the background
        // color, depending on their opacity. I.e. a 25% opaque foreground object will provide 25% of its
        // color while the background will provide the remaining 75%. The sum of the two RGBA vectors gets
        // written onto the output buffer.
        addDesiredStateChange(new EnableBlending());

        // (*) In this context SRC is Foreground. This effectively says:
        // Resulting RGB = ForegroundRGB * ForegroundAlpha + BackgroundRGB * (1 - ForegroundAlpha)
        // Which might still look complicated, but it's actually the most typical alpha-driven composite.
        // A neat tool to play with this settings can be found here: http://www.andersriggelsen.dk/glblendfunc.php
        addDesiredStateChange(new SetBlendFunction(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));

        // Important note: the following disables writing to the Depth Buffer. This is why filters relying on
        // depth information (i.e. DoF) have problems with transparent objects: the depth of their pixels is
        // found to be that of the background rather than that of the transparent's object surface.
        // This is an unresolved (unresolv-able?) issue that would only be reversed, not eliminated,
        // by re-enabling writing to the Depth Buffer.
        addDesiredStateChange(new DisableDepthWriting());
    }

    /**
     * Iterates over registered RenderSystem instances and call their renderAlphaBlend() method.
     *
     * This leaves great freedom to RenderSystem implementations, but also the responsibility to
     * leave the OpenGL state in the way they found it - otherwise the next system or the next
     * render node might not be able to function properly.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/simpleBlendMaterials");

        for (RenderSystem renderer : componentSystemManager.iterateRenderSubscribers()) {
            renderer.renderAlphaBlend();
        }

        PerformanceMonitor.endActivity();
    }
}
