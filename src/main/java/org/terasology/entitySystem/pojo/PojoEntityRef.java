package org.terasology.entitySystem.pojo;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEntityRef extends EntityRef{
    int id;
    PojoEntityManager entityManager;

    PojoEntityRef(PojoEntityManager manager, int id) {
        this.id = id;
        this.entityManager = manager;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean exists() {
        return id != PojoEntityManager.NULL_ID;
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return entityManager.getComponent(id, componentClass);
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        return entityManager.addComponent(id, component);
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        entityManager.removeComponent(id, componentClass);
    }

    @Override
    public void saveComponent(Component component) {
        entityManager.saveComponent(id, component);
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return entityManager.iterateComponents(id);
    }

    @Override
    public void destroy() {
        entityManager.destroy(id);
    }

    @Override
    public void send(Event event) {
        entityManager.getEventSystem().send(this, event);
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return entityManager.hasComponent(id, component);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof EntityRef) {
            if (!exists() && !((EntityRef) o).exists()) return true;
        }
        if (o instanceof PojoEntityRef) {
            return id == ((PojoEntityRef) o).id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return !exists() ? 0 : (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "EntityRef{" +
                "id=" + id +
                '}';
    }

    void invalidate() {
        id = PojoEntityManager.NULL_ID;
    }
}
