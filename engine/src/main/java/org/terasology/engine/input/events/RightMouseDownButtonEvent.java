// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.events;


import org.joml.Vector2i;
import org.terasology.input.MouseInput;

public final class RightMouseDownButtonEvent extends MouseDownButtonEvent {

    private static RightMouseDownButtonEvent event = new RightMouseDownButtonEvent(0);

    private RightMouseDownButtonEvent(float delta) {
        super(MouseInput.MOUSE_RIGHT, delta);
    }

    public static RightMouseDownButtonEvent create(Vector2i position, float delta) {
        event.reset(delta);
        event.setMousePosition(position);
        return event;
    }
}
