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
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.DisplayDeviceInfo;
import org.terasology.engine.subsystem.Resolution;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.DisplayModeSetting;
import org.terasology.utilities.subscribables.AbstractSubscribable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glViewport;

public class LwjglDisplayDevice extends AbstractSubscribable implements DisplayDevice {
    public static final String DISPLAY_RESOLUTION_CHANGE = "displayResolutionChange";

    private final Supplier<GLFWVidMode> desktopResolution = createDesktopResolutionSupplier();
    private final Supplier<List<GLFWVidMode>> availableResolutions = createAvailableResolutionSupplier();

    private RenderingConfig config;
    private DisplayDeviceInfo displayDeviceInfo = new DisplayDeviceInfo("unknown");

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
                GLFWVidMode vidMode = desktopResolution.get();
                GLFW.glfwSetWindowMonitor(window,
                        MemoryUtil.NULL,
                        0,
                        0,
                        vidMode.width(),
                        vidMode.height(),
                        GLFW.GLFW_DONT_CARE);
                GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
                config.setDisplayModeSetting(displayModeSetting);
                config.setWindowedFullscreen(true);
                break;
            case WINDOWED:
                GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
                GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
                GLFW.glfwSetWindowMonitor(window,
                        MemoryUtil.NULL,
                        config.getWindowPosX(),
                        config.getWindowPosY(),
                        config.getWindowWidth(),
                        config.getWindowHeight(),
                        GLFW.GLFW_DONT_CARE);
                config.setDisplayModeSetting(displayModeSetting);
                config.setFullscreen(false);
                break;
        }
        if (resize) {
            updateViewport();
        }
    }

    @Override
    public Resolution getResolution() {
        Resolution resolution = config.getResolution();
        if (resolution != null) {
            return resolution;
        }
        return new LwjglResolution(desktopResolution.get());
    }

    @Override
    public List<Resolution> getResolutions() {
        return availableResolutions.get().stream()
                .map(LwjglResolution::new)
                .collect(Collectors.toList());
    }

    @Override
    public int getWidth() {
        int[] width = new int[1];
        GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), width, new int[1]);
        return width[0];
    }

    @Override
    public int getHeight() {
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), new int[1], height);
        return height[0];
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

    @Override
    public DisplayDeviceInfo getInfo() {
        LwjglGraphicsUtil.updateDisplayDeviceInfo(displayDeviceInfo);
        return displayDeviceInfo;
    }

    public void update() {
        processMessages();
        GLFW.glfwSwapBuffers(GLFW.glfwGetCurrentContext());
    }

    private void updateViewport() {
        updateViewport(getWidth(), getHeight());
    }

    protected void updateViewport(int width, int height) {
        glViewport(0, 0, width, height);
        propertyChangeSupport.firePropertyChange(DISPLAY_RESOLUTION_CHANGE, 0, 1);
    }

    private GLFWVidMode getFullScreenDisplayMode() {
        Resolution resolution = config.getResolution();
        if (resolution instanceof LwjglResolution) {
            return getGLFWVidMode((LwjglResolution) resolution)
                    .orElseGet(desktopResolution);
        }
        return desktopResolution.get();
    }

    private void updateFullScreenDisplay() {
        long window = GLFW.glfwGetCurrentContext();
        GLFWVidMode vidMode = getFullScreenDisplayMode();
        GLFW.glfwSetWindowMonitor(window,
                GLFW.glfwGetPrimaryMonitor(),
                0,
                0,
                vidMode.width(),
                vidMode.height(),
                vidMode.refreshRate());
    }


    private static Supplier<GLFWVidMode> createDesktopResolutionSupplier() {
        return Suppliers.memoize(() -> GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()));
    }

    private Optional<GLFWVidMode> getGLFWVidMode(LwjglResolution resolution) {
        return availableResolutions.get()
                .stream()
                .filter(Predicate.isEqual(resolution))
                .findFirst();
    }

    private static Supplier<List<GLFWVidMode>> createAvailableResolutionSupplier() {
        return Suppliers.memoize(() -> GLFW.glfwGetVideoModes(GLFW.glfwGetPrimaryMonitor())
                .stream()
                .sorted(Comparator
                        .comparing(GLFWVidMode::width)
                        .thenComparing(GLFWVidMode::width)
                        .thenComparing(GLFWVidMode::refreshRate)
                )
                .collect(Collectors.toList()));
    }
}
