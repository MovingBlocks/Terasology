package org.terasology.network;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;

/**
 * @author Immortius
 */
public class NetEntityRef extends EntityRef {

    private int networkId;
    private NetworkSystem networkSystem;

    private EntityRef getActualEntityRef() {
        return networkSystem.getEntity(networkId);
    }

    @Override
    public boolean exists() {
        return getActualEntityRef().exists();
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return getActualEntityRef().hasComponent(component);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return getActualEntityRef().getComponent(componentClass);
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        return getActualEntityRef().addComponent(component);
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        getActualEntityRef().removeComponent(componentClass);
    }

    @Override
    public void saveComponent(Component component) {
        getActualEntityRef().saveComponent(component);
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return getActualEntityRef().iterateComponents();
    }

    @Override
    public void destroy() {
        getActualEntityRef().destroy();
    }

    @Override
    public void send(Event event) {
        getActualEntityRef().send(event);
    }

    @Override
    public int getId() {
        return getActualEntityRef().getId();
    }

    @Override
    public boolean isPersisted() {
        return getActualEntityRef().isPersisted();
    }

    @Override
    public void setPersisted(boolean persisted) {
        getActualEntityRef().setPersisted(persisted);
    }

    @Override
    public String getParentPrefab() {
        return getActualEntityRef().getParentPrefab();
    }
}
