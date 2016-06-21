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

import org.lwjgl.opengl.GL13;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.nui.properties.Range;
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
public class BloomPassesNode implements Node {

    @Range(min = 0.0f, max = 5.0f)
    private float bloomHighPassThreshold = 0.05f;

    @Range(min = 0.0f, max = 32.0f)
    private float bloomBlurRadius = 12.0f;

    @In
    private Config config;

    @In
    private FrameBuffersManager frameBuffersManager;

    @In
    private WorldRenderer worldRenderer;

    private RenderingConfig renderingConfig;
    private Material highPass;
    private Material blur;
    private FBO sceneOpaque;
    private FBO sceneBloom0;
    private FBO sceneBloom1;
    private FBO sceneBloom2;
    private FBO sceneHighPass;

    @Override
    public void initialise() {
        renderingConfig = config.getRendering();
        blur = worldRenderer.getMaterial("engine:prog.blur");
        highPass = worldRenderer.getMaterial("engine:prog.highp"); // TODO: rename shader to highPass
    }

    /**
     * If bloom is enabled via the rendering settings, this method generates the images needed
     * for the bloom shader effect and stores them in their own frame buffers.
     * <p>
     * This effects renders adds fringes (or "feathers") of light to areas of intense brightness.
     * This in turn give the impression of those areas partially overwhelming the camera or the eye.
     * <p>
     * For more information see: http://en.wikipedia.org/wiki/Bloom_(shader_effect)
     */
    @Override
    public void process() {
        if (renderingConfig.isBloom()) {
            PerformanceMonitor.startActivity("rendering/bloompasses");

            sceneBloom0 = frameBuffersManager.getFBO("sceneBloom0");
            sceneBloom1 = frameBuffersManager.getFBO("sceneBloom1");
            sceneBloom2 = frameBuffersManager.getFBO("sceneBloom2");
            sceneHighPass = frameBuffersManager.getFBO("sceneHighPass");
            sceneOpaque = frameBuffersManager.getFBO("sceneOpaque");

            generateHighPass();
            generateBloom(sceneBloom0);
            generateBloom(sceneBloom1);
            generateBloom(sceneBloom2);

            PerformanceMonitor.endActivity();
        }
    }

    private void generateHighPass() {
        highPass.enable();
        highPass.setFloat("highPassThreshold", bloomHighPassThreshold, true);

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneOpaque.bindTexture();
        highPass.setInt("tex", texId);

        // TODO: Investigate why this is here
//        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
//        buffers.sceneOpaque.bindDepthTexture();
//        program.setInt("texDepth", texId++);

        sceneHighPass.bind();

        setViewportToSizeOf(sceneHighPass);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        bindDisplay();
        setViewportToSizeOf(sceneOpaque);
    }

    private void generateBloom(FBO sceneBloom) {
        blur.enable();
        blur.setFloat("radius", bloomBlurRadius, true);
        blur.setFloat2("texelSize", 1.0f / sceneBloom.width(), 1.0f / sceneBloom.height(), true);

        if (sceneBloom == sceneBloom0) {
            sceneHighPass.bindTexture();
        } else if (sceneBloom == sceneBloom1) {
            sceneBloom0.bindTexture();
        } else {
            sceneBloom1.bindTexture();
        }

        sceneBloom.bind();

        setViewportToSizeOf(sceneBloom);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        bindDisplay();     // TODO: verify this is necessary
        setViewportToSizeOf(sceneOpaque);    // TODO: verify this is necessary
    }

}
