package org.terasology.entitySystem;

/**
 * Marker interface for classes that can be sent to entities as events
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface Event {
    public void cancel();
    public boolean isCancelled();
}
