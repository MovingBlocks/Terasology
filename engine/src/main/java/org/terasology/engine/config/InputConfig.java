// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config;

import org.terasology.engine.context.internal.MockContext;

public class InputConfig {

    private ControllerConfig controllers = new ControllerConfig();
    private float mouseSensitivity;
    private boolean mouseYAxisInverted;

    public ControllerConfig getControllers() {
        return controllers;
    }

    public float getMouseSensitivity() {
        return mouseSensitivity;
    }

    public void setMouseSensitivity(float mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }

    public void reset() {
        Config defaultConfig = new Config(new MockContext());
        defaultConfig.loadDefaults();
        InputConfig defaultInputConfig = defaultConfig.getInput();

        setMouseSensitivity(defaultInputConfig.mouseSensitivity);
        setMouseYAxisInverted(defaultInputConfig.mouseYAxisInverted);
    }

    public boolean isMouseYAxisInverted() {
        return mouseYAxisInverted;
    }

    public void setMouseYAxisInverted(boolean mouseYAxisInverted) {
        this.mouseYAxisInverted = mouseYAxisInverted;
    }

}
