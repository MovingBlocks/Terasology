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
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.opengl.DefaultDynamicFBOs;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DynamicFBOsManager;
import org.terasology.rendering.world.WorldRenderer;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.bindDisplay;
import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO: Add diagram of this node
 */
public class InitialPostProcessingNode extends AbstractNode {
    public static final ResourceUrn SCENE_PRE_POST_URN = new ResourceUrn("engine:scenePrePost");

    @In
    private Config config;

    @In
    private DynamicFBOsManager dynamicFBOsManager;

    @In
    private WorldRenderer worldRenderer;

    private FBO scenePrePost;
    private FBO sceneOpaque;
    private Material initialPost;

    @Override
    public void initialise() {
        initialPost = worldRenderer.getMaterial("engine:prog.prePost"); // TODO: rename shader to scenePrePost
        requireFBO(new FBOConfig(SCENE_PRE_POST_URN, 1.0f, FBO.Type.HDR), dynamicFBOsManager);

    }

    /**
     * Adds chromatic aberration, light shafts, 1/8th resolution bloom, vignette onto the rendering achieved so far.
     * Stores the result into its own buffer to be used at a later stage.
     */
    @Override
    public void process() {
        // Initial Post-Processing: chromatic aberration, light shafts, 1/8th resolution bloom, vignette
        PerformanceMonitor.startActivity("rendering/initialPostProcessing");
        scenePrePost = dynamicFBOsManager.get(SCENE_PRE_POST_URN);
        sceneOpaque = dynamicFBOsManager.get(DefaultDynamicFBOs.READ_ONLY_GBUFFER.getName());
        initialPost.enable();

        // TODO: verify what the inputs are
        scenePrePost.bind(); // TODO: see if we could write this straight into sceneOpaque

        setViewportToSizeOf(scenePrePost);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        bindDisplay();     // TODO: verify this is necessary
        setViewportToSizeOf(sceneOpaque);    // TODO: verify this is necessary

        PerformanceMonitor.endActivity();
    }
}
