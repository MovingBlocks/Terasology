/*
 * Copyright 2013 Moving Blocks
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

import org.terasology.input.events.ButtonEvent;

/**
 * @author Immortius
 */
public class BindButtonEvent extends ButtonEvent {

    private String id;
    private ButtonState state;

    public BindButtonEvent() {
        super(0);
    }

    void prepare(String id, ButtonState state, float delta) {
        reset(delta);
        this.id = id;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public ButtonState getState() {
        return state;
    }

    @Override
    public String getButtonName() {
        return "bind:" + id;
    }
}
