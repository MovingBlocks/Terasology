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


import org.terasology.math.geom.Vector2i;

/**
 */
public class MouseWheelEvent extends InputEvent {

    private int wheelTurns;
    private Vector2i mousePosition = new Vector2i();

    public MouseWheelEvent(Vector2i mousePosition, int wheelTurns, float delta) {
        super(delta);
        this.wheelTurns = wheelTurns;
        this.mousePosition.set(mousePosition);
    }

    public int getWheelTurns() {
        return wheelTurns;
    }

    public Vector2i getMousePosition() {
        return mousePosition;
    }
}
