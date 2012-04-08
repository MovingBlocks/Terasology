package org.terasology.entitySystem.pojo.persistence;

import org.terasology.entitySystem.*;
import org.terasology.entitySystem.pojo.PojoEntityRef;
import org.terasology.protobuf.EntityData;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface EntityPersister {
    <T> void registerTypeHandler(Class<? extends T> forClass, TypeHandler<T> handler);

    void registerComponentClass(Class<? extends Component> componentClass);

    EntityData.Entity serializeEntity(int id, EntityRef entity);

    EntityData.Component serializeComponent(Component component);

    Component deserializeComponent(EntityData.Component componentData);

    EntityRef deserializeEntity(EntityData.Entity entity);

    Component copyComponent(Component component);

    void setPrefabManager(PrefabManager prefabManager);
    void setPersistableEntityManager(PersistableEntityManager persistableEntityManager);
}
