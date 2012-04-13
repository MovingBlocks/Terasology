package org.terasology.entitySystem.pojo.persistence;

import org.terasology.entitySystem.*;
import org.terasology.entitySystem.pojo.PojoEntityRef;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * Interface for persist Entity System related classes to and from EntityData messages.
 * @author Immortius <immortius@gmail.com>
 */
public interface EntityPersister {
    // TODO: Move these to some sort of ComponentLibrary class
    <T> void registerTypeHandler(Class<? extends T> forClass, TypeHandler<T> handler);
    <T extends Component> void registerComponentClass(Class<T> componentClass);

    /**
     * @return The serialized form of the current EntityManager's and PrefabManager's data
     */
    EntityData.World serializeWorld();

    /**
     * @param entity
     * @return The message for a single Entity
     */
    EntityData.Entity serializeEntity(EntityRef entity);

    /**
     * @param prefab
     * @return The message for a single Prefab
     */
    EntityData.Prefab serializePrefab(Prefab prefab);

    /**
     * @param component
     * @return The message for a single Component
     */
    EntityData.Component serializeComponent(Component component);

    /**
     * Deserializes a world message, applying it to the current EntityManager
     * @param world
     */
    void deserializeWorld(EntityData.World world);

    EntityRef deserializeEntity(EntityData.Entity entityData);

    Prefab deserializePrefab(EntityData.Prefab prefabData);

    Component deserializeComponent(EntityData.Component componentData);

    // TODO: Remove
    <T extends Component> T copyComponent(T component);

    /**
     * Sets the entity manager used for serialization and deserialization
     * @param entityManager
     */
    void setEntityManager(PersistableEntityManager entityManager);

    /**
     * @return The current EntityManager
     */
    PersistableEntityManager getEntityManager();

    void setPrefabManager(PrefabManager prefabManager);
    void setPersistableEntityManager(PersistableEntityManager persistableEntityManager);

    /**
     * @return Should serialization use a lookup table to map component types to indexes. This saves space in the
     * final result, but reduces readability of text formats
     */
    boolean isUsingLookupTables();

    /**
     * Sets whether serialization should use a lookup table to map component types to indexes. This saves space in
     * the final result, but reduces readability of text formats
     * @param enabled
     */
    void setUsingLookupTables(boolean enabled);

    /**
     * Sets the id table to use when encountering components with type indexes. This is useful when serializing
     * individual entities/prefabs/components.
     *
     * When using serialize/deserializeWorld, a table will automatically be generated.
     * @param componentIdTable
     */
    public void setComponentTypeIdTable(Map<Integer, Class<? extends Component>> componentIdTable);

    /**
     * Clears the current component id table
     */
    public void clearComponentTypeIdTable();
}
