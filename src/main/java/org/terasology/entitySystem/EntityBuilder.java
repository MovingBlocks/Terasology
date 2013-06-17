package org.terasology.entitySystem;

import com.google.common.collect.Maps;
import org.terasology.asset.AssetUri;

import java.util.Map;

/**
 * An entity builder provides the ability to set up an entity before creating it. This prevents events being sent
 * for components being added or modified before it is fully set up.
 * @author Immortius
 */
public class EntityBuilder implements ComponentContainer {

    private Map<Class<? extends Component>, Component> components = Maps.newHashMap();
    private EngineEntityManager manager;

    public EntityBuilder(EngineEntityManager manager) {
        this.manager = manager;
    }

    /**
     * Produces an entity with the components contained in this entity builder
     * @return The built entity.
     */
    public EntityRef build() {
        return manager.create(components.values());
    }

    public EntityRef buildNoEvents() {
        return manager.createEntityWithoutEvents(components.values());
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return components.keySet().contains(component);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return (T) components.get(componentClass);
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        components.put(component.getClass(), component);
        return component;
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        components.remove(componentClass);
    }

    @Override
    public void saveComponent(Component component) {
        components.put(component.getClass(), component);
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return  components.values();
    }

}
