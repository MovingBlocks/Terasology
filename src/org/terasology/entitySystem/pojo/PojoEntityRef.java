package org.terasology.entitySystem.pojo;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEntityRef implements EntityRef{
    long id;
    PojoEntityManager entityManager;

    PojoEntityRef(PojoEntityManager manager, long id) {
        this.id = id;
        this.entityManager = manager;
    }

    public long getId() {
        return id;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        return entityManager.getComponent(id, componentClass);
    }

    public <T extends Component> T addComponent(T component) {
        return entityManager.addComponent(id, component);
    }

    public void removeComponent(Class<? extends Component> componentClass) {
        entityManager.removeComponent(id, componentClass);
    }

    public void saveComponent(Component component) {
        entityManager.saveComponent(id, component);
    }

    public Iterable<Component> iterateComponents() {
        return entityManager.iterateComponents(id);
    }

    public void destroy() {
        entityManager.destroy(id);
    }

    public void send(Event event) {
        entityManager.getEventSystem().send(this, event);
    }

    public boolean hasComponent(Class<? extends Component> component) {
        return entityManager.hasComponent(id, component);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PojoEntityRef that = (PojoEntityRef) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
