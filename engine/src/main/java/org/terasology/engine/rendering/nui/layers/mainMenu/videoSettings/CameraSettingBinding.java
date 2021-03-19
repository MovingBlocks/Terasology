// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings;

import org.terasology.engine.config.RenderingConfig;
import org.terasology.nui.databinding.Binding;

/**
 */
public class CameraSettingBinding implements Binding<CameraSetting> {

    private RenderingConfig config;

    public CameraSettingBinding(RenderingConfig config) {
        this.config = config;
    }

    @Override
    public CameraSetting get() {
        return config.getCameraSettings().getCameraSetting();
    }

    @Override
    public void set(CameraSetting value) {
        config.getCameraSettings().setCameraSetting(value);
    }
}
