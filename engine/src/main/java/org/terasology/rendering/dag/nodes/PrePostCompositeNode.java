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

import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.*;

/**
 * TODO: Add diagram of this node
 */
public class PrePostCompositeNode extends AbstractNode {

    @In
    private FrameBuffersManager frameBuffersManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    private Material prePostComposite;
    private RenderingDebugConfig renderingDebugConfig;

    public PrePostCompositeNode(String id) {
        super(id);
    }

    @Override
    public void initialise() {
        prePostComposite = worldRenderer.getMaterial("engine:prog.combine");
        renderingDebugConfig = config.getRendering().getDebug();
    }

    /**
     * Adds outlines and ambient occlusion to the rendering obtained so far stored in the primary FBO.
     * Stores the resulting output back into the primary buffer.
     */
    @Override
    public void process() {
        disableWireframeIf(renderingDebugConfig.isWireframe());
        prePostComposite.enable();
        FBO sceneOpaque = frameBuffersManager.getFBO("sceneOpaque");
        FBO sceneOpaquePingPong = frameBuffersManager.getFBO("sceneOpaquePingPong");
        FBO sceneReflectiveRefractive = frameBuffersManager.getFBO("sceneReflectiveRefractive");

        // TODO: verify if there should be bound textures here.
        sceneOpaquePingPong.bind();

        setViewportToSizeOf(sceneOpaquePingPong);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        bindDisplay();     // TODO: verify this is necessary
        setViewportToSizeOf(sceneOpaque);    // TODO: verify this is necessary

        frameBuffersManager.swapSceneOpaqueFBOs();
        sceneOpaque.attachDepthBufferTo(sceneReflectiveRefractive);
    }
}
