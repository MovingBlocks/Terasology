package org.terasology.entitySystem.common;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;

/**
 * Null entity implementation - acts the same as an empty entity, except you cannot add anything to it.
 * @author Immortius <immortius@gmail.com>
 */
public class NullEntityRef extends EntityRef {
    private static NullEntityRef instance = new NullEntityRef();

    public static NullEntityRef getInstance() {
        return instance;
    }

    private NullEntityRef() {
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return false;
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return null;
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        return null;
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
    }

    @Override
    public void saveComponent(Component component) {
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return NullIterator.newInstance();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void send(Event event) {
    }

    @Override
    public String toString() {
        return "Entity(Null)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof EntityRef) {
            return !((EntityRef) o).exists();
        }
        return o == null;
    }

    @Override
    public int hashCode() {
        return 0;
    }

}
