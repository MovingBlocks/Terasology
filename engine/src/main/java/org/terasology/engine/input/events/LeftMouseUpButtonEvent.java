// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.events;


import org.joml.Vector2i;
import org.terasology.input.MouseInput;

public final class LeftMouseUpButtonEvent extends MouseUpButtonEvent {

    private static LeftMouseUpButtonEvent event = new LeftMouseUpButtonEvent(0);

    private LeftMouseUpButtonEvent(float delta) {
        super(MouseInput.MOUSE_LEFT, delta);
    }

    public static LeftMouseUpButtonEvent create(Vector2i mousePos, float delta) {
        event.reset(delta);
        event.setMousePosition(mousePos);
        return event;
    }

}
