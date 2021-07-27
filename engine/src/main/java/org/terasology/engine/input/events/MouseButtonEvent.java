// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.events;


import org.joml.Vector2i;
import org.terasology.input.ButtonState;
import org.terasology.input.MouseInput;

public class MouseButtonEvent extends ButtonEvent {

    private MouseInput button;
    private ButtonState state;
    private Vector2i mousePosition = new Vector2i();

    public MouseButtonEvent(MouseInput button, ButtonState state, float delta) {
        super(delta);
        this.state = state;
        this.button = button;
    }

    @Override
    public ButtonState getState() {
        return state;
    }

    public MouseInput getButton() {
        return button;
    }

    public String getMouseButtonName() {
        return button.getName();
    }

    public String getButtonName() {
        return "mouse:" + getMouseButtonName();
    }

    public Vector2i getMousePosition() {
        return new Vector2i(mousePosition);
    }

    protected void setButton(MouseInput button) {
        this.button = button;
    }

    public void setMousePosition(Vector2i mousePosition) {
        this.mousePosition.set(mousePosition);
    }

    public void reset() {
        reset(0f);
    }
}
