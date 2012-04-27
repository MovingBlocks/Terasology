package org.terasology.entitySystem.event;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.Event;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class AddComponentEvent extends AbstractEvent {

    private static AddComponentEvent instance = new AddComponentEvent();
    
    public static AddComponentEvent newInstance() {
        return instance;
    }

    private AddComponentEvent() {}
}
