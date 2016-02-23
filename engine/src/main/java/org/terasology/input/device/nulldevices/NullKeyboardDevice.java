/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.input.device.nulldevices;

import com.google.common.collect.Queues;

import org.terasology.input.device.KeyboardAction;
import org.terasology.input.device.KeyboardDevice;

import java.util.Queue;

/**
 */
public class NullKeyboardDevice implements KeyboardDevice {

    @Override
    public boolean isKeyDown(int button) {
        return false;
    }

    @Override
    public Queue<KeyboardAction> getInputQueue() {
        return Queues.newArrayDeque();
    }
}
