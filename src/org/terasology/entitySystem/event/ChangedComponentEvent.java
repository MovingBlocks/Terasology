package org.terasology.entitySystem.event;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ChangedComponentEvent implements Event {

    private static ChangedComponentEvent instance = new ChangedComponentEvent();

    public static ChangedComponentEvent newInstance() {
        return instance;
    }

    private ChangedComponentEvent() {}
}
