package org.terasology.entitySystem.common;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;

/**
 * Null entity implementation - acts the same as an empty entity, except you cannot add anything to it.
 * @author Immortius <immortius@gmail.com>
 */
public class NullEntityRef implements EntityRef {
    private static NullEntityRef instance = new NullEntityRef();

    public static NullEntityRef getInstance() {
        return instance;
    }

    private NullEntityRef() {
    }

    public long getId() {
        return 0;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        return null;
    }

    public <T extends Component> T addComponent(T component) {
        return null;
    }

    public void removeComponent(Class<? extends Component> componentClass) {
    }

    public void saveComponent(Component component) {
    }

    public Iterable<Component> iterateComponents() {
        return NullIterator.newInstance();
    }

    public void destroy() {
    }

    public void send(Event event) {
    }

    public boolean hasComponent(Class<? extends Component> component) {
        return false;
    }
}
