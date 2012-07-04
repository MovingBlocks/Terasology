package org.terasology.events.input;


import org.terasology.entitySystem.EntityRef;

public class RightMouseUpButtonEvent extends MouseUpButtonEvent {

    private static RightMouseUpButtonEvent event = new RightMouseUpButtonEvent(0);

    public static RightMouseUpButtonEvent create(float delta) {
        event.reset(delta);
        return event;
    }

    private RightMouseUpButtonEvent(float delta) {
        super(1, delta);
    }

}
