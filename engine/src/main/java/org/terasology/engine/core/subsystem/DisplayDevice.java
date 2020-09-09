// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem;

import org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings.DisplayModeSetting;
import org.terasology.engine.utilities.subscribables.Subscribable;

import java.util.List;

public interface DisplayDevice extends Subscribable {

    boolean hasFocus();

    boolean isCloseRequested();

    boolean isFullscreen();

    void setFullscreen(boolean state);

    DisplayModeSetting getDisplayModeSetting();

    void setDisplayModeSetting(DisplayModeSetting displayModeSetting);

    /**
     * @return currently active full-screen resolution.
     */
    Resolution getResolution();

    /**
     * Change currently active full-screen resolution.
     *
     * @param resolution resolution to set.
     */
    void setResolution(Resolution resolution);

    /**
     * @return list of available full-screen resolutions.
     */
    List<Resolution> getResolutions();

    /**
     * @return display width
     */
    int getDisplayWidth();

    /**
     * @return display height
     */
    int getDisplayHeight();

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

}
