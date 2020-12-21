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
package org.terasology.engine.subsystem.headless.device;

import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.engine.subsystem.DisplayDeviceInfo;
import org.terasology.engine.subsystem.Resolution;
import org.terasology.engine.subsystem.lwjgl.LwjglGraphicsUtil;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.DisplayModeSetting;
import org.terasology.utilities.subscribables.AbstractSubscribable;

import java.util.Collections;
import java.util.List;

public class HeadlessDisplayDevice extends AbstractSubscribable implements DisplayDevice {

    public HeadlessDisplayDevice() {
    }

    @Override
    public boolean isHeadless() {
        return true;
    }

    @Override
    public boolean hasFocus() {

        return true;
    }

    @Override
    public boolean isCloseRequested() {
        return false;
    }

    @Override
    public void setFullscreen(boolean state) {
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
        return HeadlessResolution.getInstance();
    }

    @Override
    public List<Resolution> getResolutions() {
        return Collections.singletonList(getResolution());
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void setResolution(Resolution resolution) {
    }

    @Override
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public void processMessages() {
    }

    @Override
    public void prepareToRender() {
    }

    @Override
    public void update() {
    }

    @Override
    public DisplayDeviceInfo getInfo() {
        return new DisplayDeviceInfo("headless");
    }
}
