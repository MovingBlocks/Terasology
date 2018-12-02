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
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.Resolution;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.DisplayModeSetting;
import org.terasology.utilities.subscribables.AbstractSubscribable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return Display.isActive();
    }

    @Override
    public boolean isCloseRequested() {
        return Display.isCloseRequested();
    }

    @Override
    public boolean isFullscreen() {
        return Display.isFullscreen();
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
        try {
            switch (displayModeSetting) {
                case FULLSCREEN:
                    updateFullScreenDisplay();
                    config.setDisplayModeSetting(displayModeSetting);
                    config.setFullscreen(true);
                    break;
                case WINDOWED_FULLSCREEN:
                    System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
                    Display.setDisplayMode(Display.getDesktopDisplayMode());
                    Display.setLocation(0, 0);
                    Display.setFullscreen(false);
                    config.setDisplayModeSetting(displayModeSetting);
                    config.setWindowedFullscreen(true);
                    break;
                case WINDOWED:
                    System.setProperty("org.lwjgl.opengl.Window.undecorated", "false");
                    Display.setDisplayMode(config.getDisplayMode());
                    Display.setLocation(config.getWindowPosX(), config.getWindowPosY());
                    Display.setFullscreen(false);
                    Display.setResizable(true);
                    config.setDisplayModeSetting(displayModeSetting);
                    config.setFullscreen(false);
                    break;
            }
            if (resize) {
                glViewport(0, 0, Display.getWidth(), Display.getHeight());
            }
        } catch (LWJGLException e) {
            throw new RuntimeException("Can not initialize graphics device.", e);
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
    public void setResolution(Resolution resolution) {
        config.setResolution(resolution);
        if (DisplayModeSetting.FULLSCREEN == config.getDisplayModeSetting()) {
            try {
                updateFullScreenDisplay();
                updateViewport();
            } catch (LWJGLException e) {
                throw new RuntimeException("Can not set resolution", e);
            }
        }
    }

    @Override
    public void processMessages() {
        Display.processMessages();
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
        if (Display.wasResized()) {
            updateViewport();
            // Note that the "old" and "new" values (0 and 1) in the above call aren't actually
            // used: they are only necessary to ensure that the event is fired up correctly.
        }
    }

    private void updateViewport() {
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        propertyChangeSupport.firePropertyChange(DISPLAY_RESOLUTION_CHANGE, 0, 1);
    }

    private DisplayMode getFullScreenDisplayMode() {
        Resolution resolution = config.getResolution();
        if (resolution instanceof LwjglResolution) {
            return ((LwjglResolution) resolution).getDisplayMode();
        }
        return Display.getDesktopDisplayMode();
    }

    private void updateFullScreenDisplay() throws LWJGLException {
        Display.setDisplayMode(getFullScreenDisplayMode());
        Display.setFullscreen(true);
        Display.setLocation(0, 0);
    }

    private static Supplier<Resolution> createDesktopResolutionSupplier() {
        return Suppliers.memoize(() -> new LwjglResolution(Display.getDesktopDisplayMode()));
    }

    private static Supplier<List<Resolution>> createAvailableResolutionSupplier() {
        return Suppliers.memoize(() -> {
            try {
                return Stream
                        .of(Display.getAvailableDisplayModes())
                        .filter(DisplayMode::isFullscreenCapable)
                        .sorted(Comparator
                                .comparing(DisplayMode::getWidth)
                                .thenComparing(DisplayMode::getHeight)
                                .thenComparing(DisplayMode::getBitsPerPixel)
                                .thenComparing(DisplayMode::getFrequency)
                        )
                        .map(LwjglResolution::new)
                        .collect(Collectors.toList());
            } catch (LWJGLException e) {
                throw new RuntimeException("Can not get available resolutions.", e);
            }
        });
    }
}
