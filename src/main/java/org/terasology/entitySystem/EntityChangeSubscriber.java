package org.terasology.entitySystem;

/**
 * @author Immortius
 */
public interface EntityChangeSubscriber {
    void onEntityChange(EntityRef entity, Class<? extends Component> component);
}
