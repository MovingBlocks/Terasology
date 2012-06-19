package org.terasology.entitySystem.persistence;

import org.terasology.entitySystem.*;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * Interface for persist Entity System related classes to and from EntityData messages.
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface EntityPersisterHelper {

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
     *
     * @param world
     */
    void deserializeWorld(EntityData.World world);

    EntityRef deserializeEntity(EntityData.Entity entityData);

    Prefab deserializePrefab(EntityData.Prefab prefabData);

    /**
     * Deserializes a prefab, adjusting the prefab's name (and parent's names) to be within the supplied
     * package context if necessary.
     *
     * @param prefabData
     * @param packageContext
     * @return The deserialized prefab
     */
    Prefab deserializePrefab(EntityData.Prefab prefabData, String packageContext);

    Component deserializeComponent(EntityData.Component componentData);

    /**
     * @return Should serialization use a lookup table to map component types to indexes. This saves space in the
     *         final result, but reduces readability of text formats
     */
    boolean isUsingLookupTables();

    /**
     * Sets whether serialization should use a lookup table to map component types to indexes. This saves space in
     * the final result, but reduces readability of text formats
     *
     * @param enabled
     */
    void setUsingLookupTables(boolean enabled);

    /**
     * Sets the id table to use when encountering components with type indexes. This is useful when serializing
     * individual entities/prefabs/components.
     * <p/>
     * When using serialize/deserializeWorld, a table will automatically be generated.
     *
     * @param componentIdTable
     */
    public void setComponentTypeIdTable(Map<Integer, Class<? extends Component>> componentIdTable);

    /**
     * Clears the current component id table
     */
    public void clearComponentTypeIdTable();

    public EntityManager getEntityManager();

    public PrefabManager getPrefabManager();

    public ComponentLibrary getComponentLibrary();

    public void setEntityManager(PersistableEntityManager entityManager);

    public void setPrefabManager(PrefabManager prefabManager);

    public void setComponentLibrary(ComponentLibrary componentLibrary);


}