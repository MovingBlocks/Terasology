package org.terasology.events.input;


public class LeftMouseDownButtonEvent extends MouseDownButtonEvent {

    private static LeftMouseDownButtonEvent event = new LeftMouseDownButtonEvent(0);

    public static LeftMouseDownButtonEvent create(float delta) {
        event.reset(delta);
        return event;
    }

    private LeftMouseDownButtonEvent(float delta) {
        super(0, delta);
    }
}
