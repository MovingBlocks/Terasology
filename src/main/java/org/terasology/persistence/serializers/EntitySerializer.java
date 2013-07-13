/*
 * Copyright 2013 Moving Blocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.persistence.serializers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.internal.EntityInfoComponent;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.MetadataUtil;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
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
 *
 * @author Immortius
 */
public class EntitySerializer {
    private EngineEntityManager entityManager;
    private PrefabManager prefabManager;
    private ComponentLibrary componentLibrary;
    private ComponentSerializer componentSerializer;

    private ComponentSerializeCheck componentSerializeCheck = ComponentSerializeCheck.NullCheck.create();
    private boolean ignoringEntityId = false;

    /**
     * @param entityManager The entityManager that deserialized entities will be placed in.
     */
    public EntitySerializer(EngineEntityManager entityManager) {
        this(entityManager, entityManager.getComponentLibrary());
    }

    public EntitySerializer(EngineEntityManager entityManager, ComponentLibrary componentLibrary) {
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

    public boolean isUsingFieldIds() {
        return componentSerializer.isUsingFieldIds();
    }

    public void setUsingFieldIds(boolean usingFieldIds) {
        componentSerializer.setUsingFieldIds(usingFieldIds);
    }

    public void setComponentSerializeCheck(ComponentSerializeCheck check) {
        this.componentSerializeCheck = check;
    }

    /**
     * Sets whether entity ids should be ignored during serialization and deserialization.
     * If ignored, then they will not be stored in the protobuf, and deserialized entities will be given new ids. This
     * may break entity references, depending on how they are being handled (see: EntityRefTypeHandler)
     *
     * @param ignoringEntityId
     */
    public void setIgnoringEntityId(boolean ignoringEntityId) {
        this.ignoringEntityId = ignoringEntityId;
    }

    /**
     * Sets the mapping between component classes and the ids that are used for serialization
     *
     * @param table
     */
    public void setComponentIdMapping(Map<Class<? extends Component>, Integer> table) {
        componentSerializer.setIdMapping(table);
    }

    /**
     * @return An immutable copy of the component id mapping
     */
    public Map<Class<? extends Component>, Integer> getComponentIdMapping() {
        return componentSerializer.getIdMapping();
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
        Map<Class<? extends Component>, Component> componentMap = createInitialComponents(entityData);
        deserializeOntoComponents(entityData, componentMap);
        if (ignoringEntityId) {
            return entityManager.create(componentMap.values());
        } else {
            return entityManager.createEntityWithId(entityData.getId(), componentMap.values());
        }
    }

    /**
     * Creates the components for the entity being deserialized based on its prefab (if any)
     *
     * @param entityData
     * @return The mapping of components
     */
    private Map<Class<? extends Component>, Component> createInitialComponents(EntityData.Entity entityData) {
        Map<Class<? extends Component>, Component> componentMap = Maps.newHashMap();
        if (entityData.hasParentPrefab() && !entityData.getParentPrefab().isEmpty() && prefabManager.exists(entityData.getParentPrefab())) {
            Prefab prefab = prefabManager.getPrefab(entityData.getParentPrefab());
            for (Component component : prefab.iterateComponents()) {
                String componentName = MetadataUtil.getComponentClassName(component.getClass());
                // TODO: Use a set and convert to lower case
                if (!containsIgnoreCase(componentName, entityData.getRemovedComponentList())) {
                    componentMap.put(component.getClass(), componentLibrary.copy(component));
                }
            }
            componentMap.put(EntityInfoComponent.class, new EntityInfoComponent(entityData.getParentPrefab(), true, prefab.isAlwaysRelevant()));
        }
        return componentMap;
    }

    /**
     * Deserializes the components from an EntityData onto a map of components
     *
     * @param entityData
     * @param componentMap
     */
    private void deserializeOntoComponents(EntityData.Entity entityData, Map<Class<? extends Component>, Component> componentMap) {
        EntityInfoComponent entityInfo = (EntityInfoComponent)componentMap.get(EntityInfoComponent.class);
        if (entityInfo == null) {
            entityInfo = new EntityInfoComponent();
            componentMap.put(EntityInfoComponent.class, entityInfo);
        }
        if (entityData.hasOwner()) {
            entityInfo.owner = entityManager.getEntity(entityData.getOwner());
        }
        if (entityData.hasAlwaysRelevant()) {
            entityInfo.alwaysRelevant = entityData.getAlwaysRelevant();
        }

        for (EntityData.Component componentData : entityData.getComponentList()) {
            ComponentMetadata<? extends Component> metadata = componentSerializer.getComponentMetadata(componentData);
            if (metadata == null || !componentSerializeCheck.serialize(metadata)) {
                continue;
            }

            Component existingComponent = componentMap.get(metadata.getType());
            if (existingComponent == null) {
                Component newComponent = componentSerializer.deserialize(componentData);
                componentMap.put(metadata.getType(), newComponent);
            } else {
                componentSerializer.deserializeOnto(existingComponent, componentData, FieldSerializeCheck.NullCheck.<Component>newInstance());
            }
        }
    }

    private EntityData.Entity serializeEntityFull(EntityRef entityRef, FieldSerializeCheck<Component> fieldCheck) {
        EntityData.Entity.Builder entity = EntityData.Entity.newBuilder();
        if (!ignoringEntityId) {
            entity.setId(entityRef.getId());
        }
        entity.setAlwaysRelevant(entityRef.isAlwaysRelevant());
        EntityRef owner = entityRef.getOwner();
        if (owner.exists()) {
            entity.setOwner(owner.getId());
        }
        for (Component component : entityRef.iterateComponents()) {
            if (!componentSerializeCheck.serialize(componentLibrary.getMetadata(component.getClass()))) {
                continue;
            }

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
        if (entityRef.isAlwaysRelevant() != prefab.isAlwaysRelevant()) {
            entity.setAlwaysRelevant(entityRef.isAlwaysRelevant());
        }
        EntityRef owner = entityRef.getOwner();
        if (owner.exists()) {
            entity.setOwner(owner.getId());
        }
        Set<Class<? extends Component>> presentClasses = Sets.newHashSet();
        for (Component component : entityRef.iterateComponents()) {
            if (!componentSerializeCheck.serialize(componentLibrary.getMetadata(component.getClass()))) {
                continue;
            }

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
        for (Component prefabComponent : prefab.iterateComponents()) {
            if (!presentClasses.contains(prefabComponent.getClass()) && componentSerializeCheck.serialize(componentLibrary.getMetadata(prefabComponent.getClass()))) {
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
