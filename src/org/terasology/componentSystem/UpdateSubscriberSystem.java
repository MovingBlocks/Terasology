package org.terasology.componentSystem;

import org.terasology.entitySystem.ComponentSystem;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface UpdateSubscriberSystem extends ComponentSystem {
    public void update(float delta);
}
