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
package org.terasology.input.events;


import org.terasology.input.ButtonState;

public class MouseDownButtonEvent extends MouseButtonEvent {

    private static MouseDownButtonEvent event = new MouseDownButtonEvent(0, 0);

    public static MouseDownButtonEvent create(int button, float delta) {
        event.reset(delta);
        event.setButton(button);
        return event;
    }

    protected MouseDownButtonEvent(int button, float delta) {
        super(button, ButtonState.DOWN, delta);
    }

}
