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

/**
 * @author Immortius
 */
public class InputConfig {

    private BindsConfig binds = new BindsConfig();
    private float mouseSensitivity = 0.075f;
    private boolean mouseYAxisInverted = false;

    public BindsConfig getBinds() {
        return binds;
    }

    public float getMouseSensitivity() {
        return mouseSensitivity;
    }

    public void setMouseSensitivity(float mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }

    public void reset() {
        binds.setBinds(BindsConfig.createDefault());
        InputConfig defaultConfig = new InputConfig();
        setMouseSensitivity(defaultConfig.mouseSensitivity);
        setMouseYAxisInverted(defaultConfig.mouseYAxisInverted);
    }

    public boolean isMouseYAxisInverted() {
        return mouseYAxisInverted;
    }

    public void setMouseYAxisInverted(boolean mouseYAxisInverted) {

        this.mouseYAxisInverted = mouseYAxisInverted;

    }

}
