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
package org.terasology.input.events;


import org.terasology.input.ButtonState;
import org.terasology.input.MouseInput;
import org.terasology.math.geom.Vector2i;

public class MouseDownButtonEvent extends MouseButtonEvent {

    private static MouseDownButtonEvent event = new MouseDownButtonEvent(MouseInput.NONE, 0);

    protected MouseDownButtonEvent(MouseInput button, float delta) {
        super(button, ButtonState.DOWN, delta);
    }

    public static MouseDownButtonEvent create(MouseInput button, Vector2i mousePos, float delta) {
        event.reset(delta);
        event.setButton(button);
        event.setMousePosition(mousePos);
        return event;
    }
}
