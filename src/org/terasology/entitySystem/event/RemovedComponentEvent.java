package org.terasology.entitySystem.event;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class RemovedComponentEvent implements Event {

    private static RemovedComponentEvent instance = new RemovedComponentEvent();

    public static RemovedComponentEvent newInstance() {
        return instance;
    }

    private RemovedComponentEvent() {}
}
