package org.terasology.logic.console;

import org.terasology.entitySystem.AbstractEvent;

/**
 * @author Immortius
 */
public abstract class MessageEvent extends AbstractEvent {

    public abstract String getFormattedMessage();
}
