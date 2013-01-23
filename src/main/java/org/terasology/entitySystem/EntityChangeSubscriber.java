package org.terasology.entitySystem;

/**
 * @author Immortius
 */
public interface EntityChangeSubscriber {
    void onEntityComponentAdded(EntityRef entity, Class<? extends Component> component);
    void onEntityComponentRemoved(EntityRef entity, Class<? extends Component> component);
    void onEntityComponentChange(EntityRef entity, Class<? extends Component> component);
}
