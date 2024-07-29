// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem;

import org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings.DisplayModeSetting;
import org.terasology.engine.utilities.subscribables.Subscribable;
import org.terasology.context.annotation.API;

import java.util.List;

@API
public interface DisplayDevice extends Subscribable {

    boolean hasFocus();

    boolean isCloseRequested();

    void setFullscreen(boolean state);

    boolean isFullscreen();

    void setDisplayModeSetting(DisplayModeSetting displayModeSetting);

    DisplayModeSetting getDisplayModeSetting();

    /**
     * @return currently active full-screen resolution.
     */
    Resolution getResolution();

    /**
     * @return list of available full-screen resolutions.
     */
    List<Resolution> getResolutions();

    /**
     * @return display width
     */
    int getWidth();

    /**
     * @return display height
     */
    int getHeight();

    /**
     * Change currently active full-screen resolution.
     *
     * @param resolution resolution to set.
     */
    void setResolution(Resolution resolution);

    // TODO: this breaks the nice API we have so far.
    // From the lwjgl docs:
    //   Process operating system events.
    //   Call this to update the Display's state and to receive new input device events.
    //   This method is called from update(), so it is not necessary to call this method
    //   if update() is called periodically.
    void processMessages();

    boolean isHeadless();

    // TODO: another method that possibly doesn't need to exist, but I need to check with Immortius on this
    void prepareToRender();

    void update();

    DisplayDeviceInfo getInfo();
}
