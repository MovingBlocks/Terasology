// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.cameras;

import org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings.CameraSetting;

public class PerspectiveCameraSettings {
    private CameraSetting cameraSetting;

    public PerspectiveCameraSettings(CameraSetting cameraSetting) {
        this.cameraSetting = cameraSetting;
    }

    public CameraSetting getCameraSetting() {
        return cameraSetting;
    }

    public void setCameraSetting(CameraSetting cameraSetting) {
        this.cameraSetting = cameraSetting;
    }

    public int getSmoothingFramesCount() {
        return cameraSetting.getSmoothingFrames();
    }
}
