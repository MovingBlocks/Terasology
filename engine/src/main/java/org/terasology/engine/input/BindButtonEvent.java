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

package org.terasology.input;

import org.terasology.engine.SimpleUri;
import org.terasology.input.events.ButtonEvent;

/**
 */
public class BindButtonEvent extends ButtonEvent {

    private SimpleUri id;
    private ButtonState state;

    public BindButtonEvent() {
        super(0);
    }

    public void prepare(SimpleUri buttonId, ButtonState newState, float delta) {
        reset(delta);
        this.id = buttonId;
        this.state = newState;
    }

    public SimpleUri getId() {
        return id;
    }

    @Override
    public ButtonState getState() {
        return state;
    }

}
