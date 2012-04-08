package org.terasology.entitySystem.pojo.persistence;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.pojo.PojoEntityRef;
import org.terasology.protobuf.EntityData;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface EntityPersister {
    <T> void registerTypeHandler(Class<? extends T> forClass, TypeHandler<T> handler);

    void registerComponentClass(Class<? extends Component> componentClass);

    EntityData.Entity serializeEntity(int id, EntityRef entity);

    EntityData.Entity serializeEntity(int id, EntityRef entity, Prefab prefab);

    EntityData.Component serializeComponent(Component component);

    Component deserializeComponent(EntityData.Component componentData);

    Component copyComponent(Component component);
}
