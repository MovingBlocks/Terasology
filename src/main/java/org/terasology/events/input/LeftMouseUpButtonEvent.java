package org.terasology.events.input;


import org.terasology.entitySystem.EntityRef;

public class LeftMouseUpButtonEvent extends MouseUpButtonEvent {

    private static LeftMouseUpButtonEvent event = new LeftMouseUpButtonEvent(0, EntityRef.NULL);

    public static LeftMouseUpButtonEvent create(float delta, EntityRef target) {
        event.reset(delta, target);
        return event;
    }

    private LeftMouseUpButtonEvent(float delta, EntityRef target) {
        super(0, delta, target);
    }

}
