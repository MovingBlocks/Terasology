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
package org.terasology.engine.subsystem.headless;

public class HeadlessDisplay implements org.terasology.engine.subsystem.Display {

    public HeadlessDisplay() {
    }

    @Override
    public boolean isActive() {

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
    public void resizeViewport() {
    }

    @Override
    public boolean wasResized() {
        return false;
    }

    @Override
    public void processMessages() {
    }
}
