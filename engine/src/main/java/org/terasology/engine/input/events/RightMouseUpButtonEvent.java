// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.events;


import org.joml.Vector2i;
import org.terasology.input.MouseInput;

public final class RightMouseUpButtonEvent extends MouseUpButtonEvent {

    private static RightMouseUpButtonEvent event = new RightMouseUpButtonEvent(0);

    private RightMouseUpButtonEvent(float delta) {
        super(MouseInput.MOUSE_RIGHT, delta);
    }

    public static RightMouseUpButtonEvent create(Vector2i mousePos, float delta) {
        event.reset(delta);
        event.setMousePosition(mousePos);
        return event;
    }


}
