package org.terasology.entitySystem.componentSystem;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface UpdateSubscriberSystem extends ComponentSystem {
    public void update(float delta);
}
