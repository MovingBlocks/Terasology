package org.terasology.entitySystem.event;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.Event;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class RemovedComponentEvent extends AbstractEvent {

    private static RemovedComponentEvent instance = new RemovedComponentEvent();

    public static RemovedComponentEvent newInstance() {
        return instance;
    }

    private RemovedComponentEvent() {}
}
