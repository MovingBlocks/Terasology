package org.terasology.input;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * Event when the camera ceases to be over an entity - sent to the involved entity
 */
public class CameraOutEvent extends AbstractEvent {

    public CameraOutEvent() {
    }
}
