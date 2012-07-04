package org.terasology.events.input;


import org.terasology.entitySystem.EntityRef;

public class LeftMouseUpButtonEvent extends MouseUpButtonEvent {

    private static LeftMouseUpButtonEvent event = new LeftMouseUpButtonEvent(0);

    public static LeftMouseUpButtonEvent create(float delta) {
        event.reset(delta);
        return event;
    }

    private LeftMouseUpButtonEvent(float delta) {
        super(0, delta);
    }

}
