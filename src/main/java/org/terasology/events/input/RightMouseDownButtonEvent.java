package org.terasology.events.input;


import org.terasology.entitySystem.EntityRef;

public class RightMouseDownButtonEvent extends MouseDownButtonEvent {

    private static RightMouseDownButtonEvent event = new RightMouseDownButtonEvent(0, EntityRef.NULL);

    public static RightMouseDownButtonEvent create(float delta, EntityRef target) {
        event.reset(delta, target);
        return event;
    }

    private RightMouseDownButtonEvent(float delta, EntityRef target) {
        super(1, delta, target);
    }
}
