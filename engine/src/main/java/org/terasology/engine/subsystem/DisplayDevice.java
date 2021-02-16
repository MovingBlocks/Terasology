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
package org.terasology.engine.subsystem;

import org.terasology.module.sandbox.API;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.DisplayModeSetting;
import org.terasology.utilities.subscribables.Subscribable;

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
