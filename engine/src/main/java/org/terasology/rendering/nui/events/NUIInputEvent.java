/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.nui.events;

import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.MouseDevice;

/**
 * The NUIManager has it's own event classes, so that it is independent from the entity event system.
 * The event class contains all the information needed to process the event. It is abstract as each event type
 * should have it's own class so that it is later on possible to add fields to a certain event without breaking
 * signatures.
 */
public abstract class NUIInputEvent {
    private MouseDevice mouse;
    private KeyboardDevice keyboard;

    public NUIInputEvent(MouseDevice mouse, KeyboardDevice keyboard) {
        this.mouse = mouse;
        this.keyboard = keyboard;
    }

    public MouseDevice getMouse() {
        return mouse;
    }

    public KeyboardDevice getKeyboard() {
        return keyboard;
    }
}
