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

public class HeadlessDisplayDevice implements DisplayDevice {

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
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public void processMessages() {
    }

    @Override
    public void prepareToRender() {
    }
}
