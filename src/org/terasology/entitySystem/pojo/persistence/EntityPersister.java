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

    EntityData.World serializeWorld();

    EntityData.Entity serializeEntity(EntityRef entity);

    EntityData.Prefab serializePrefab(Prefab prefab);

    EntityData.Component serializeComponent(Component component);

    void deserializeWorld(EntityData.World world);

    EntityRef deserializeEntity(EntityData.Entity entityData);

    Prefab deserializePrefab(EntityData.Prefab prefabData);

    Component deserializeComponent(EntityData.Component componentData);

    Component copyComponent(Component component);

    void setPrefabManager(PrefabManager prefabManager);
    void setPersistableEntityManager(PersistableEntityManager persistableEntityManager);
}
