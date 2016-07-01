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
import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO: Add diagram of this node
 */
public class ToneMappedSceneNode implements Node {
    @In
    private FrameBuffersManager frameBuffersManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    private Material toneMapping;
    private FBO sceneToneMapped;
    private FBO sceneOpaque;

    @Override
    public void initialise() {
        toneMapping = worldRenderer.getMaterial("engine:prog.hdr"); // TODO: rename shader to toneMapping)
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
        PerformanceMonitor.startActivity("rendering/tonemapped");
        sceneToneMapped = frameBuffersManager.getFBO("sceneToneMapped");
        sceneOpaque = frameBuffersManager.getFBO("sceneOpaque");

        toneMapping.enable();

        sceneToneMapped.bind();
        setViewportToSizeOf(sceneToneMapped);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);     // TODO: verify this is necessary

        renderFullscreenQuad();

        bindDisplay();     // TODO: verify this is necessary
        setViewportToSizeOf(sceneOpaque);    // TODO: verify this is necessary

        PerformanceMonitor.endActivity();
    }
}
