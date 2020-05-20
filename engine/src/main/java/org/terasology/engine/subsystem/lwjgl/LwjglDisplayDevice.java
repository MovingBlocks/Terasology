/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem.lwjgl;

import com.google.common.base.Suppliers;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.Resolution;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.DisplayModeSetting;
import org.terasology.utilities.subscribables.AbstractSubscribable;

import java.nio.IntBuffer;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glViewport;

public class LwjglDisplayDevice extends AbstractSubscribable implements DisplayDevice {
    public static final String DISPLAY_RESOLUTION_CHANGE = "displayResolutionChange";

    private final Supplier<Resolution> desktopResolution = createDesktopResolutionSupplier();
    private final Supplier<List<Resolution>> availableResolutions = createAvailableResolutionSupplier();

    private RenderingConfig config;

    public LwjglDisplayDevice(Context context) {
        this.config = context.get(Config.class).getRendering();
    }

    @Override
    public boolean hasFocus() {
        return GLFW.GLFW_TRUE == GLFW.glfwGetWindowAttrib(GLFW.glfwGetCurrentContext(), GLFW.GLFW_FOCUSED);
    }

    @Override
    public boolean isCloseRequested() {
        return GLFW.glfwWindowShouldClose(GLFW.glfwGetCurrentContext());
    }

    @Override
    public boolean isFullscreen() {
        return MemoryUtil.NULL != GLFW.glfwGetWindowMonitor(GLFW.glfwGetCurrentContext());
    }

    @Override
    public void setFullscreen(boolean state) {
        if (state) {
            setDisplayModeSetting(DisplayModeSetting.FULLSCREEN, true);
        } else {
            setDisplayModeSetting(DisplayModeSetting.WINDOWED, true);
        }
    }

    @Override
    public DisplayModeSetting getDisplayModeSetting() {
        return config.getDisplayModeSetting();
    }

    @Override
    public void setDisplayModeSetting(DisplayModeSetting displayModeSetting) {
        setDisplayModeSetting(displayModeSetting, true);
    }

    public void setDisplayModeSetting(DisplayModeSetting displayModeSetting, boolean resize) {
        long window = GLFW.glfwGetCurrentContext();
        switch (displayModeSetting) {
            case FULLSCREEN:
                updateFullScreenDisplay();
                config.setDisplayModeSetting(displayModeSetting);
                config.setFullscreen(true);
                break;
            case WINDOWED_FULLSCREEN:
                GLFWVidMode vidMode = ((LwjglResolution) getResolution()).getVidMode();
                GLFW.glfwSetWindowMonitor(window,
                        MemoryUtil.NULL,
                        0,
                        0,
                        vidMode.width(),
                        vidMode.height(),
                        vidMode.refreshRate());
                GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
                config.setDisplayModeSetting(displayModeSetting);
                config.setWindowedFullscreen(true);
                break;
            case WINDOWED:
                GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
                GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
                GLFW.glfwSetWindowPos(window, config.getWindowPosX(), config.getWindowPosY());
                GLFW.glfwSetWindowSize(window, config.getWindowWidth(), config.getWindowHeight());
                config.setDisplayModeSetting(displayModeSetting);
                config.setFullscreen(false);
                break;
        }
        if (resize) {
            glViewport(0, 0, this.getDisplayWidth(), this.getDisplayHeight());
        }
    }

    @Override
    public Resolution getResolution() {
        Resolution resolution = config.getResolution();
        if (resolution != null) {
            return resolution;
        }
        return desktopResolution.get();
    }

    @Override
    public List<Resolution> getResolutions() {
        return availableResolutions.get();
    }

    @Override
    public int getDisplayWidth() {
        return ((LwjglResolution) getResolution()).getVidMode().width();
    }

    @Override
    public int getDisplayHeight() {
        return ((LwjglResolution) getResolution()).getVidMode().height();
    }

    @Override
    public void setResolution(Resolution resolution) {
        config.setResolution(resolution);
        if (DisplayModeSetting.FULLSCREEN == config.getDisplayModeSetting()) {
            updateFullScreenDisplay();
            updateViewport();
        }
    }

    @Override
    public void processMessages() {
        GLFW.glfwPollEvents();
    }

    @Override
    public boolean isHeadless() {
        return false;
    }

    @Override
    public void prepareToRender() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();
    }

    public void update() {
        processMessages();
        GLFW.glfwSwapBuffers(GLFW.glfwGetCurrentContext());
    }

    private void updateViewport() {
        IntBuffer widthBuffer = IntBuffer.allocate(1);
        IntBuffer heightBuffer = IntBuffer.allocate(1);
        GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), widthBuffer, heightBuffer);
        updateViewport(widthBuffer.get(), heightBuffer.get());
    }

    protected void updateViewport(int width, int height) {
        glViewport(0, 0, width, height);
        propertyChangeSupport.firePropertyChange(DISPLAY_RESOLUTION_CHANGE, 0, 1);
    }

    private GLFWVidMode getFullScreenDisplayMode() {
        Resolution resolution = config.getResolution();
        if (resolution instanceof LwjglResolution) {
            return ((LwjglResolution) resolution).getVidMode();
        }
        return GLFW.glfwGetVideoMode(0);
    }

    private void updateFullScreenDisplay() {
        long window = GLFW.glfwGetCurrentContext();
        GLFWVidMode vidMode = ((LwjglResolution) getResolution()).getVidMode();
        GLFW.glfwSetWindowMonitor(window,
                GLFW.glfwGetPrimaryMonitor(),
                0,
                0,
                vidMode.width(),
                vidMode.height(),
                vidMode.refreshRate());
    }

    private static Supplier<Resolution> createDesktopResolutionSupplier() {
        return Suppliers.memoize(() -> new LwjglResolution(GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())));
    }

    private static Supplier<List<Resolution>> createAvailableResolutionSupplier() {
        return Suppliers.memoize(() -> GLFW.glfwGetVideoModes(GLFW.glfwGetPrimaryMonitor())
                .stream() // FIXME possible npe
                .sorted(Comparator
                        .comparing(GLFWVidMode::width)
                        .thenComparing(GLFWVidMode::width)
                        //.thenComparing(DisplayMode::getBitsPerPixel) // DPI in other place
                        .thenComparing(GLFWVidMode::refreshRate)
                )
                .map(LwjglResolution::new)
                .collect(Collectors.toList()));
    }
}
