/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.input;

import java.util.List;
import java.util.Queue;

import org.terasology.input.device.ControllerAction;
import org.terasology.input.device.InputDevice;

/**
 * Represents <b>all</b> connected controllers (e.g. gamepads, but also some keyboards).
 * Unfortunately, it is impossible to separate events in LWJGL2 based on controllers, so they have
 * to be processes all at once.
 */
public interface ControllerDevice extends InputDevice {

    @Override
    Queue<ControllerAction> getInputQueue();

    /**
     * @return a list of currently connected controller IDs.
     */
    List<String> getControllers();
}
