package org.terasology.entitySystem.event;

/**
 * A consumable event is an event that can be prevented from continuing through remaining event receivers. This is
 * primarily useful for input event.
 *
 * @author Immortius
 */
public interface ConsumableEvent extends Event {

    public boolean isConsumed();

    public void consume();
}
