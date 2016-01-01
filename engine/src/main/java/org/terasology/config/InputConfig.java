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

import org.terasology.context.Context;

public class InputConfig {

    private BindsConfig binds = new BindsConfig();
    private ControllerConfig controllers = new ControllerConfig();
    private float mouseSensitivity;
    private boolean mouseYAxisInverted;

    public BindsConfig getBinds() {
        return binds;
    }

    public ControllerConfig getControllers() {
        return controllers;
    }

    public float getMouseSensitivity() {
        return mouseSensitivity;
    }

    public void setMouseSensitivity(float mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }

    public void reset(Context context) {
        binds.setBinds(BindsConfig.createDefault(context));

        Config defaultConfig = new Config();
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
