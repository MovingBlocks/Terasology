package org.terasology.entitySystem.prefab;

import com.google.common.collect.Maps;
import org.terasology.asset.AssetData;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.MutableComponentContainer;

import java.util.Map;

/**
 * @author Immortius
 */
public class PrefabData implements AssetData, MutableComponentContainer {

    private Map<Class<? extends Component>, Component> components = Maps.newHashMap();
    private boolean persisted = true;
    private Prefab parent;

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
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return components.containsKey(component);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return components.values();
    }

    public Map<Class<? extends Component>, Component> getComponents() {
        return components;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    public void setParent(Prefab parent) {
        this.parent = parent;
    }

    public Prefab getParent() {
        return parent;
    }
}