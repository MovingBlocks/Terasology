package org.terasology.events.input;


import org.terasology.entitySystem.EntityRef;

public class RightMouseUpButtonEvent extends MouseUpButtonEvent {

    private static RightMouseUpButtonEvent event = new RightMouseUpButtonEvent(0, EntityRef.NULL);

    public static RightMouseUpButtonEvent create(float delta, EntityRef target) {
        event.reset(delta, target);
        return event;
    }

    private RightMouseUpButtonEvent(float delta, EntityRef target) {
        super(1, delta, target);
    }

}
