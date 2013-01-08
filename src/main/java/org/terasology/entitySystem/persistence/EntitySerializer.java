package org.terasology.entitySystem.persistence;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityInfoComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.MetadataUtil;
import org.terasology.protobuf.EntityData;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Provides the ability to serialize and deserialize entities to the EntityData.Entity proto buffer format.
 * <p/>
 * As with the component serializer, a component id mapping can be provided to have components serialized against
 * ids rather than name strings.
 * <p/>
 * It is also possible to set whether entity ids will be handled or ignored - if ignored then deserialized entities will
 * be given new ids.
 * @author Immortius
 */
public class EntitySerializer {
    private PersistableEntityManager entityManager;
    private PrefabManager prefabManager;
    private ComponentLibrary componentLibrary;
    private ComponentSerializer componentSerializer;

    private boolean ignoringEntityId = false;

    /**
     * @param entityManager The entityManager that deserialized entities will be placed in.
     */
    public EntitySerializer(PersistableEntityManager entityManager) {
        this(entityManager, entityManager.getComponentLibrary());
    }

    public EntitySerializer(PersistableEntityManager entityManager, ComponentLibrary componentLibrary) {
        this.entityManager = entityManager;
        this.prefabManager = entityManager.getPrefabManager();
        this.componentLibrary = entityManager.getComponentLibrary();
        this.componentSerializer = new ComponentSerializer(componentLibrary);
    }

    /**
     * @return Whether entity ids are being ignored during serialization/deserialization
     */
    public boolean isIgnoringEntityId() {
        return ignoringEntityId;
    }

    /**
     * Sets whether entity ids should be ignored during serialization and deserialization.
     * If ignored, then they will not be stored in the protobuf, and deserialized entities will be given new ids. This
     * may break entity references, depending on how they are being handled (see: EntityRefTypeHandler)
     * @param ignoringEntityId
     */
    public void setIgnoringEntityId(boolean ignoringEntityId) {
        this.ignoringEntityId = ignoringEntityId;
    }

    /**
     * Sets the mapping between component classes and the ids that are used for serialization
     * @param table
     */
    public void setComponentIdMapping(Map<Class<? extends Component>, Integer> table) {
        componentSerializer.setIdMapping(table);
    }

    /**
     * Clears the mapping between component classes and ids. This causes components to be serialized with their component
     * class name instead.
     */
    public void removeComponentIdMapping() {
        componentSerializer.removeIdMapping();
    }

    /**
     * @param entityRef
     * @return The serialized entity
     */
    public EntityData.Entity serialize(EntityRef entityRef) {
        return serialize(entityRef, true, FieldSerializeCheck.NullCheck.<Component>newInstance());
    }

    /**
     * @param entityRef
     * @param deltaAgainstPrefab Whether the serialized entity should be a delta against its prefab (if any)
     * @return The serialized entity
     */
    public EntityData.Entity serialize(EntityRef entityRef, boolean deltaAgainstPrefab) {
        return serialize(entityRef, deltaAgainstPrefab, FieldSerializeCheck.NullCheck.<Component>newInstance());
    }

    /**
     * @param entityRef
     * @param fieldCheck Used to check whether each field in each component of the entity should be serialized.
     * @return The serialized entity
     */
    public EntityData.Entity serialize(EntityRef entityRef, FieldSerializeCheck<Component> fieldCheck) {
        return serialize(entityRef, true, fieldCheck);
    }

    /**
     * @param entityRef
     * @param fieldCheck Used to check whether each field in each component of the entity should be serialized.
     * @return The serialized entity
     */
    public EntityData.Entity serialize(EntityRef entityRef, boolean deltaAgainstPrefab, FieldSerializeCheck<Component> fieldCheck) {
        Prefab prefab = entityRef.getParentPrefab();
        if (prefab != null && deltaAgainstPrefab) {
            return serializeEntityDelta(entityRef, prefab, fieldCheck);
        } else {
            return serializeEntityFull(entityRef, fieldCheck);
        }
    }

    /**
     * @param entityData
     * @return The deserialized entity
     */
    public EntityRef deserialize(EntityData.Entity entityData) {
        Map<Class<? extends Component>, Component> componentMap = Maps.newHashMap();
        if (entityData.hasParentPrefab() && !entityData.getParentPrefab().isEmpty() && prefabManager.exists(entityData.getParentPrefab())) {
            Prefab prefab = prefabManager.getPrefab(entityData.getParentPrefab());
            for (Component component : prefab.listComponents()) {
                String componentName = MetadataUtil.getComponentClassName(component.getClass());
                // TODO: Use a set and convert to lower case
                if (!containsIgnoreCase(componentName, entityData.getRemovedComponentList())) {
                    componentMap.put(component.getClass(), componentLibrary.copy(component));
                }
            }
            componentMap.put(EntityInfoComponent.class, new EntityInfoComponent(entityData.getParentPrefab(), true));
        }
        for (EntityData.Component componentData : entityData.getComponentList()) {
            Class<? extends Component> componentClass = componentSerializer.getComponentClass(componentData);
            if (componentClass == null) continue;

            Component existingComponent = componentMap.get(componentClass);
            if (existingComponent == null) {
                Component newComponent = componentSerializer.deserialize(componentData);
                componentMap.put(componentClass, newComponent);
            } else {
                componentSerializer.deserializeOnto(existingComponent, componentData);
            }
        }
        if (ignoringEntityId) {
            return entityManager.create(componentMap.values());
        } else {
            return entityManager.createEntityWithId(entityData.getId(), componentMap.values());
        }
    }

    public void deserializeOnto(EntityRef currentEntity, EntityData.Entity entityData) {
        Set<Class<? extends Component>> presentComponents = Sets.newLinkedHashSet();
        for (EntityData.Component componentData : entityData.getComponentList()) {
            Class<? extends Component> componentClass = componentSerializer.getComponentClass(componentData);
            if (componentClass == null) continue;
            presentComponents.add(componentClass);

            Component existingComponent = currentEntity.getComponent(componentClass);
            if (existingComponent == null) {
                currentEntity.addComponent(componentSerializer.deserialize(componentData));
            } else {
                componentSerializer.deserializeOnto(existingComponent, componentData);
                currentEntity.saveComponent(existingComponent);
            }
        }
        for (Component comp : currentEntity.iterateComponents()) {
            if (!presentComponents.contains(comp.getClass()) && comp.getClass() != EntityInfoComponent.class) {
                currentEntity.removeComponent(comp.getClass());
            }
        }
    }

    private EntityData.Entity serializeEntityFull(EntityRef entityRef, FieldSerializeCheck<Component> fieldCheck) {
        EntityData.Entity.Builder entity = EntityData.Entity.newBuilder();
        if (!ignoringEntityId) {
            entity.setId(entityRef.getId());
        }
        for (Component component : entityRef.iterateComponents()) {
            if (component.getClass().equals(EntityInfoComponent.class))
                continue;

            EntityData.Component componentData = componentSerializer.serialize(component, fieldCheck);
            if (componentData != null) {
                entity.addComponent(componentData);
            }
        }
        return entity.build();
    }

    private EntityData.Entity serializeEntityDelta(EntityRef entityRef, Prefab prefab, FieldSerializeCheck<Component> fieldCheck) {
        EntityData.Entity.Builder entity = EntityData.Entity.newBuilder();
        if (!ignoringEntityId) {
            entity.setId(entityRef.getId());
        }
        entity.setParentPrefab(prefab.getName());
        Set<Class<? extends Component>> presentClasses = Sets.newHashSet();
        for (Component component : entityRef.iterateComponents()) {
            if (component.getClass().equals(EntityInfoComponent.class))
                continue;

            presentClasses.add(component.getClass());

            Component prefabComponent = prefab.getComponent(component.getClass());
            EntityData.Component componentData;
            if (prefabComponent == null) {
                componentData = componentSerializer.serialize(component, fieldCheck);
            } else {
                componentData = componentSerializer.serialize(prefabComponent, component, fieldCheck);
            }

            if (componentData != null) {
                entity.addComponent(componentData);
            }
        }
        for (Component prefabComponent : prefab.listComponents()) {
            if (!presentClasses.contains(prefabComponent.getClass())) {
                entity.addRemovedComponent(MetadataUtil.getComponentClassName(prefabComponent.getClass()));
            }
        }
        return entity.build();
    }

    private boolean containsIgnoreCase(String componentName, List<String> removedComponentList) {
        String lowerCaseName = componentName.toLowerCase(Locale.ENGLISH);
        for (String removed : removedComponentList) {
            if (lowerCaseName.equals(removed.toLowerCase(Locale.ENGLISH))) {
                return true;
            }
        }
        return false;
    }
}
