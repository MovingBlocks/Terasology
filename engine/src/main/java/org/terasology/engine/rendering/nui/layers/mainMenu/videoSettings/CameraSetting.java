// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings;

public enum CameraSetting {
    NORMAL("${engine:menu#camera-setting-normal}", 1),
    SMOOTH("${engine:menu#camera-setting-smooth}", 5),
    CINEMATIC("${engine:menu#camera-setting-cinematic}", 60);

    private String displayName;
    private int smoothingFrames;

    CameraSetting(String displayName, int smoothingFrames) {
        this.displayName = displayName;
        this.smoothingFrames = smoothingFrames;
    }

    public int getSmoothingFrames() {
        return smoothingFrames;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
