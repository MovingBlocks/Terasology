// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.editor.subsystem;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.DisplayDeviceInfo;
import org.terasology.engine.subsystem.Resolution;
import org.terasology.engine.subsystem.lwjgl.LwjglDisplayDevice;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphicsManager;
import org.terasology.engine.subsystem.lwjgl.LwjglResolution;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.DisplayModeSetting;
import org.terasology.utilities.subscribables.AbstractSubscribable;

import java.awt.GraphicsEnvironment;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class LwjglPortletDisplayDevice extends AbstractSubscribable implements DisplayDevice {

    private final AWTGLCanvas canvas;
    private final LwjglGraphicsManager graphics;

    public LwjglPortletDisplayDevice(AWTGLCanvas canvas, LwjglGraphicsManager graphics) {
        this.canvas = canvas;
        this.graphics = graphics;
        canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateViewport();
            }
        });
    }

    @Override
    public boolean hasFocus() {
        return canvas.hasFocus();
    }

    @Override
    public boolean isCloseRequested() {
        return false;
    }

    @Override
    public void setFullscreen(boolean state) {
    }

    @Override
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public void setDisplayModeSetting(DisplayModeSetting displayModeSetting) {
    }

    @Override
    public DisplayModeSetting getDisplayModeSetting() {
        return DisplayModeSetting.WINDOWED;
    }

    @Override
    public Resolution getResolution() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        int bitDepth = env.getDefaultScreenDevice().getDisplayMode().getBitDepth();
        int refreshRate = env.getDefaultScreenDevice().getDisplayMode().getRefreshRate();
        return new LwjglResolution(getWidth(), getHeight(), bitDepth, bitDepth, bitDepth, refreshRate);
    }

    @Override
    public List<Resolution> getResolutions() {
        ArrayList<Resolution> resolutions = new ArrayList<>();
        resolutions.add(getResolution());
        return resolutions;
    }

    @Override
    public int getWidth() {
        return canvas.getWidth();
    }

    @Override
    public int getHeight() {
        return canvas.getHeight();
    }

    @Override
    public void setResolution(Resolution resolution) {
    }

    @Override
    public void processMessages() {
    }

    @Override
    public boolean isHeadless() {
        return false;
    }

    @Override
    public void prepareToRender() {
    }

    private void updateViewport() {
        updateViewport(getWidth(), getHeight());
    }

    protected void updateViewport(int width, int height) {
        graphics.asynchToDisplayThread(() -> {
            GL11.glViewport(0, 0, width, height);
            propertyChangeSupport.firePropertyChange(LwjglDisplayDevice.DISPLAY_RESOLUTION_CHANGE, 0, 1);
        });
    }

    @Override
    public void update() {
        processMessages();
        canvas.swapBuffers();
    }

    @Override
    public DisplayDeviceInfo getInfo() {
        return graphics.getDisplayDeviceInfo();
    }
}
