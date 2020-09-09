// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.events;

/**
 * Event when the mouse moved along one axis. If the mouse moved diagonal, this will be reflected in two separate
 * events.
 */
public class MouseAxisEvent extends AxisEvent {

    private static final MouseAxisEvent event = new MouseAxisEvent(MouseAxis.X, 0, 0);
    private float value;
    private MouseAxis mouseAxis;
    protected MouseAxisEvent(MouseAxis axis, float value, float delta) {
        super(delta);
        this.mouseAxis = axis;
        this.value = value;
    }

    public static MouseAxisEvent create(MouseAxis axis, float value, float delta) {
        event.reset(delta);
        event.mouseAxis = axis;
        event.value = value;
        return event;
    }

    public static MouseAxisEvent createCopy(MouseAxisEvent toBeCopied) {
        return new MouseAxisEvent(toBeCopied.getMouseAxis(), toBeCopied.getValue(), toBeCopied.getDelta());
    }

    public MouseAxis getMouseAxis() {
        return mouseAxis;
    }

    @Override
    public float getValue() {
        return value;
    }

    public enum MouseAxis {
        X, Y
    }
}
