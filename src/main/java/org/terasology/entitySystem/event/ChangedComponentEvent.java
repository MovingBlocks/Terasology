package org.terasology.entitySystem.event;

import org.terasology.entitySystem.AbstractEvent;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ChangedComponentEvent extends AbstractEvent {

    private static ChangedComponentEvent instance = new ChangedComponentEvent();

    public static ChangedComponentEvent newInstance() {
        return instance;
    }

    private ChangedComponentEvent() {}
}
