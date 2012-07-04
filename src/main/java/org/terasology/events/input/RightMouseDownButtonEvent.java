package org.terasology.events.input;


import org.terasology.entitySystem.EntityRef;

public class RightMouseDownButtonEvent extends MouseDownButtonEvent {

    private static RightMouseDownButtonEvent event = new RightMouseDownButtonEvent(0);

    public static RightMouseDownButtonEvent create(float delta) {
        event.reset(delta);
        return event;
    }

    private RightMouseDownButtonEvent(float delta) {
        super(1, delta);
    }
}
