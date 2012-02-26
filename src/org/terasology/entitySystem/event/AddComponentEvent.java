package org.terasology.entitySystem.event;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.logic.entities.StaticEntity;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class AddComponentEvent implements Event {

    private static AddComponentEvent instance = new AddComponentEvent();
    
    public static AddComponentEvent newInstance() {
        return instance;
    }

    private AddComponentEvent() {}
}
