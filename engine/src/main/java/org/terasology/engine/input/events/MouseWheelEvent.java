// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.events;


import org.joml.Vector2i;

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
