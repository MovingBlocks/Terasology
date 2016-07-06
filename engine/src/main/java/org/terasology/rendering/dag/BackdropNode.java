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
import org.terasology.config.RenderingDebugConfig;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.backdrop.BackdropRenderer;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.world.WorldRenderer;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.bindDisplay;
import static org.terasology.rendering.opengl.OpenGLUtils.disableWireframeIf;
import static org.terasology.rendering.opengl.OpenGLUtils.enableWireframeIf;
import static org.terasology.rendering.opengl.OpenGLUtils.setRenderBufferMask;

/**
 * TODO: Diagram of this node
 */
public class BackdropNode implements Node {

    @In
    private Config config;

    @In
    private BackdropRenderer backdropRenderer;

    @In
    private WorldRenderer worldRenderer;

    @In
    private FrameBuffersManager frameBuffersManager;

    private RenderingDebugConfig renderingDebugConfig;
    private Camera playerCamera;
    private FBO sceneOpaque;
    private FBO sceneReflectiveRefractive;

    @Override
    public void initialise() {
        renderingDebugConfig = config.getRendering().getDebug();
        playerCamera = worldRenderer.getActiveCamera();
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/backdrop");
        enableWireframeIf(renderingDebugConfig.isWireframe());

        sceneOpaque = frameBuffersManager.getFBO("sceneOpaque");

        initialClearing();

        sceneOpaque.bind();
        setRenderBufferMask(sceneOpaque, true, true, true);

        playerCamera.lookThroughNormalized();
        /**
         * Sets the state to render the Backdrop. At this stage the backdrop is the SkySphere
         * plus the SkyBands passes.
         *
         * The backdrop is the only rendering that has three state-changing methods.
         * This is due to the SkySphere requiring a state and the SkyBands requiring a slightly
         * different one.
         */
        setRenderBufferMask(sceneOpaque, true, false, false);
        backdropRenderer.render(playerCamera);

        disableWireframeIf(renderingDebugConfig.isWireframe());
        PerformanceMonitor.endActivity();
    }

    /**
     * Initial clearing of a couple of important Frame Buffers. Then binds back the Display.
     */
    // It's unclear why these buffers need to be cleared while all the others don't...
    private void initialClearing() {
        sceneReflectiveRefractive = frameBuffersManager.getFBO("sceneReflectiveRefractive");
        sceneOpaque.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        sceneReflectiveRefractive.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        bindDisplay();
    }
}
