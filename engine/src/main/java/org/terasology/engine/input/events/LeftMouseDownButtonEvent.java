// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.events;


import org.joml.Vector2i;
import org.terasology.nui.input.MouseInput;

public final class LeftMouseDownButtonEvent extends MouseDownButtonEvent {

    private static final LeftMouseDownButtonEvent event = new LeftMouseDownButtonEvent(0);

    private LeftMouseDownButtonEvent(float delta) {
        super(MouseInput.MOUSE_LEFT, delta);
    }

    public static LeftMouseDownButtonEvent create(Vector2i mousePosition, float delta) {
        event.reset(delta);
        event.setMousePosition(mousePosition);
        return event;
    }
}
