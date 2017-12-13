/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.config;

import org.terasology.context.internal.MockContext;

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
