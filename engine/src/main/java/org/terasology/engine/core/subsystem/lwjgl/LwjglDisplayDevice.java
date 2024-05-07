// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import com.google.common.base.Suppliers;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.core.subsystem.DisplayDeviceInfo;
import org.terasology.engine.core.subsystem.Resolution;
import org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings.DisplayModeSetting;
import org.terasology.engine.utilities.subscribables.AbstractSubscribable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glViewport;

public class LwjglDisplayDevice extends AbstractSubscribable implements DisplayDevice {
    public static final String DISPLAY_RESOLUTION_CHANGE = "displayResolutionChange";

    private final Supplier<GLFWVidMode> desktopResolution = createDesktopResolutionSupplier();
    private final Supplier<List<GLFWVidMode>> availableResolutions = createAvailableResolutionSupplier();

    private RenderingConfig config;
    private DisplayDeviceInfo displayDeviceInfo = new DisplayDeviceInfo("unknown");

    private int windowWidth = 0;
    private int windowHeight = 0;
    private boolean isWindowDirty = true;

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
                // Attempt to go into fullscreen twice to fix the taskbar showing on-top of the game on Windows.
                // See also: https://github.com/MovingBlocks/Terasology/issues/5228.
                for (int i = 0; i < 2; i++) {
                    GLFW.glfwSetWindowMonitor(window,
                            MemoryUtil.NULL,
                            0,
                            0,
                            vidMode.width(),
                            vidMode.height(),
                            GLFW.GLFW_DONT_CARE);
                    GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
                }
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
        updateWindow();
        return this.windowWidth;
    }

    @Override
    public int getHeight() {
        updateWindow();
        return this.windowHeight;
    }

    private void updateWindow() {
        if (isWindowDirty) {
            int[] windowWidth = new int[1];
            int[] windowHeight = new int[1];
            GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), windowWidth, windowHeight);
            this.windowWidth = windowWidth[0];
            this.windowHeight = windowHeight[0];
            isWindowDirty = false;
        }
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
    }

    @Override
    public DisplayDeviceInfo getInfo() {
        LwjglGraphicsUtil.updateDisplayDeviceInfo(displayDeviceInfo);
        return displayDeviceInfo;
    }

    public void update() {
        processMessages();
        GLFW.glfwSwapBuffers(GLFW.glfwGetCurrentContext());
        isWindowDirty = true;

    }

    private void updateViewport() {
        updateViewport(getWidth(), getHeight());
    }

    protected void updateViewport(int width, int height) {
        glViewport(0, 0, width, height);
        
        //If the screen is minimized, resolution change is stopped to avoid the width and height of FBO being set to 0.
        boolean isMinimized = GLFW.glfwGetWindowAttrib(GLFW.glfwGetCurrentContext(), GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE;
        int i = isMinimized ? 0 : 1;       
        propertyChangeSupport.firePropertyChange(DISPLAY_RESOLUTION_CHANGE, i, 1);
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
