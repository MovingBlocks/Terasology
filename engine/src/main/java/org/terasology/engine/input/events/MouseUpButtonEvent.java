// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.events;


import org.joml.Vector2i;
import org.terasology.input.ButtonState;
import org.terasology.input.MouseInput;

public class MouseUpButtonEvent extends MouseButtonEvent {

    private static MouseUpButtonEvent event = new MouseUpButtonEvent(MouseInput.NONE, 0);

    protected MouseUpButtonEvent(MouseInput button, float delta) {
        super(button, ButtonState.UP, delta);
    }

    public static MouseUpButtonEvent create(MouseInput button, Vector2i position, float delta) {
        event.reset(delta);
        event.setButton(button);
        event.setMousePosition(position);
        return event;
    }


}
