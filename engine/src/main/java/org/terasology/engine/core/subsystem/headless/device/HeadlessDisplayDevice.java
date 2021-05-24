// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.device;

import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.core.subsystem.DisplayDeviceInfo;
import org.terasology.engine.core.subsystem.Resolution;
import org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings.DisplayModeSetting;
import org.terasology.engine.utilities.subscribables.AbstractSubscribable;

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
