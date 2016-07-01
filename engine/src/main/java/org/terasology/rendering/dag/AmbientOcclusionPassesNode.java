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
package org.terasology.rendering.dag;

import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.bindDisplay;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;

/**
 * TODO: Add diagram of this node
 */
public class AmbientOcclusionPassesNode implements Node {

    @In
    private FrameBuffersManager frameBuffersManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    private RenderingConfig renderingConfig;
    private FBO sceneOpaque;
    private FBO ssaoBlurredFBO;
    private FBO ssaoFBO;
    private Material ssaoShader;
    private Material ssaoBlurredShader;

    @Override
    public void initialise() {
        renderingConfig = config.getRendering();
        ssaoShader = worldRenderer.getMaterial("engine:prog.ssao");
        ssaoBlurredShader = worldRenderer.getMaterial("engine:prog.ssaoBlur");
    }

    /**
     * If Ambient Occlusion is enabled in the render settings, this method generates and
     * stores the necessary images into their own FBOs. The stored images are eventually
     * combined with others.
     * <p>
     * For further information on Ambient Occlusion see: http://en.wikipedia.org/wiki/Ambient_occlusion
     */
    @Override
    public void process() {
        if (renderingConfig.isSsao()) {
            PerformanceMonitor.startActivity("rendering/ambientocclusionpasses");
            // TODO: consider moving these into initialise without breaking existing implementation
            sceneOpaque = frameBuffersManager.getFBO("sceneOpaque");
            ssaoBlurredFBO = frameBuffersManager.getFBO("ssaoBlurred");
            ssaoFBO = frameBuffersManager.getFBO("ssao");

            generateSSAO();
            generateBlurredSSAO();
            PerformanceMonitor.endActivity();
        }
    }

    private void generateSSAO() {
        ssaoShader.enable();
        ssaoShader.setFloat2("texelSize", 1.0f / ssaoFBO.width(), 1.0f / ssaoFBO.height(), true);
        ssaoShader.setFloat2("noiseTexelSize", 1.0f / 4.0f, 1.0f / 4.0f, true);

        // TODO: verify if some textures should be bound here
        ssaoFBO.bind();

        setViewportToSizeOf(ssaoFBO);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        bindDisplay(); // TODO: verify this is necessary
        setViewportToSizeOf(sceneOpaque); // TODO: verify this is necessary
    }

    private void generateBlurredSSAO() {
        ssaoBlurredShader.enable();
        ssaoBlurredShader.setFloat2("texelSize", 1.0f / ssaoBlurredFBO.width(), 1.0f / ssaoBlurredFBO.height(), true);

        ssaoFBO.bindTexture(); // TODO: verify this is the only input

        ssaoBlurredFBO.bind();

        setViewportToSizeOf(ssaoBlurredFBO);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        bindDisplay(); // TODO: verify this is necessary
        setViewportToSizeOf(sceneOpaque); // TODO: verify this is necessary
    }
}
