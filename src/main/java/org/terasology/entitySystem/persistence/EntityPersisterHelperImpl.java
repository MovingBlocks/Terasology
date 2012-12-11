/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.entitySystem.persistence;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityInfoComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.ComponentUtil;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.protobuf.EntityData;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntityPersisterHelperImpl implements EntityPersisterHelper {
    private static final Logger logger = LoggerFactory.getLogger(EntityPersisterHelperImpl.class);

    private ComponentLibrary componentLibrary;
    private PrefabManager prefabManager;
    private PersistableEntityManager entityManager;

    private boolean useLookupTables = false;

    BiMap<Integer, Class<? extends Component>> componentIdTable = HashBiMap.create();

    public EntityPersisterHelperImpl(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
    }

    public EntityPersisterHelperImpl(ComponentLibrary componentLibrary, PrefabManager prefabManager) {
        this.componentLibrary = componentLibrary;
        this.prefabManager = prefabManager;
    }

    public EntityPersisterHelperImpl(PersistableEntityManager entityManager) {
        this.componentLibrary = entityManager.getComponentLibrary();
        this.entityManager = entityManager;
        this.prefabManager = entityManager.getPrefabManager();
    }

    @Override
    public EntityData.World serializeWorld() {
        final EntityData.World.Builder world = EntityData.World.newBuilder();

        if (isUsingLookupTables()) {
            writeComponentTypeTable(world);
        }

        for (Prefab prefab : prefabManager.listPrefabs()) {
            world.addPrefab(serializePrefab(prefab));
        }

        TIntList nonPersistedIds = new TIntArrayList();
        for (EntityRef entity : entityManager.iteratorEntities()) {
            if (entity.isPersisted()) {
                world.addEntity(serializeEntity(entity));
            } else {
                nonPersistedIds.add(entity.getId());
            }
        }

        writeIdInfo(world, nonPersistedIds);
        return world.build();
    }

    @Override
    public EntityData.Entity serializeEntity(EntityRef entityRef) {
        EntityInfoComponent entityInfo = entityRef.getComponent(EntityInfoComponent.class);
        if (entityInfo != null) {
            if (entityInfo.parentPrefab != null && prefabManager.exists(entityInfo.parentPrefab)) {
                return serializeEntityDelta(entityRef, prefabManager.getPrefab(entityInfo.parentPrefab));
            }
        }
        return serializeEntityFull(entityRef);
    }

    @Override
    public EntityData.Prefab serializePrefab(Prefab prefab) {
        EntityData.Prefab.Builder prefabData = EntityData.Prefab.newBuilder();
        prefabData.setName(prefab.getName());
        for (Prefab parent : prefab.getParents()) {
            prefabData.addParentName(parent.getName());
        }
        prefabData.setPersisted(prefab.isPersisted());

        for (Component component : prefab.listOwnComponents()) {
            EntityData.Component componentData = serializeComponent(component);
            if (componentData != null) {
                prefabData.addComponent(componentData);
            }
        }
        return prefabData.build();
    }

    @Override
    public EntityData.Component serializeComponent(Component component) {
        ComponentMetadata<?> componentMetadata = componentLibrary.getMetadata(component.getClass());
        if (componentMetadata == null) {
            logger.error("Unregistered component type: {}", component.getClass());
            return null;
        }
        EntityData.Component.Builder componentMessage = EntityData.Component.newBuilder();
        if (useLookupTables) {
            componentMessage.setTypeIndex(componentIdTable.inverse().get(component.getClass()));
        } else {
            componentMessage.setType(ComponentUtil.getComponentClassName(component));
        }

        for (FieldMetadata field : componentMetadata.iterateFields()) {
            try {
                Object rawValue = field.getValue(component);
                if (rawValue == null) continue;

                EntityData.Value value = field.serialize(rawValue);
                if (value == null) continue;

                componentMessage.addField(EntityData.NameValue.newBuilder().setName(field.getName()).setValue(value).build());
            } catch (IllegalAccessException e) {
                logger.error("Exception during serializing component type: {}", component.getClass(), e);
            } catch (InvocationTargetException e) {
                logger.error("Exception during serializing component type: {}", component.getClass(), e);
            }
        }

        return componentMessage.build();
    }

    @Override
    public void deserializeWorld(EntityData.World world) {
        entityManager.setNextId(world.getNextEntityId());
        for (Integer deadId : world.getFreedEntityIdList()) {
            entityManager.getFreedIds().add(deadId);
        }

        for (EntityData.Prefab prefabData : world.getPrefabList()) {
            if (!prefabManager.exists(prefabData.getName())) {
                deserializePrefab(prefabData);
            }
        }

        for (int index = 0; index < world.getComponentClassCount(); ++index) {
            ComponentMetadata componentMetadata = componentLibrary.getMetadata(world.getComponentClass(index));
            if (componentMetadata != null) {
                componentIdTable.put(index, componentMetadata.getType());
            }
        }

        for (EntityData.Entity entityData : world.getEntityList()) {
            deserializeEntity(entityData);
        }
    }

    @Override
    public EntityRef deserializeEntity(EntityData.Entity entityData) {
        Map<Class<? extends Component>, Component> componentMap = Maps.newHashMap();
        if (entityData.hasParentPrefab() && !entityData.getParentPrefab().isEmpty() && prefabManager.exists(entityData.getParentPrefab())) {
            Prefab prefab = prefabManager.getPrefab(entityData.getParentPrefab());
            for (Component component : prefab.listComponents()) {
                String componentName = ComponentUtil.getComponentClassName(component.getClass());
                if (!containsIgnoreCase(componentName, entityData.getRemovedComponentList())) {
                    componentMap.put(component.getClass(), componentLibrary.copy(component));
                }
            }
            componentMap.put(EntityInfoComponent.class, new EntityInfoComponent(entityData.getParentPrefab(), true));
        }
        for (EntityData.Component componentData : entityData.getComponentList()) {
            Class<? extends Component> componentClass = getComponentClass(componentData);
            if (componentClass == null) continue;

            Component existingComponent = componentMap.get(componentClass);
            if (existingComponent == null) {
                Component newComponent = deserializeComponent(componentData);
                componentMap.put(componentClass, newComponent);
            } else {
                deserializeComponentOnto(existingComponent, componentData);
            }
        }
        return entityManager.createEntityWithId(entityData.getId(), componentMap.values());
    }

    @Override
    public Prefab deserializePrefab(EntityData.Prefab prefabData, AssetUri uri) {
        String name = uri.getSimpleString();

        Prefab prefab = prefabManager.createPrefab(name);
        prefab.setPersisted(prefabData.getPersisted());
        for (String parentName : prefabData.getParentNameList()) {
            int packageSplit = parentName.indexOf(':');
            if (packageSplit == -1) {
                parentName = uri.getPackage() + ":" + parentName;
            }
            Prefab parent = prefabManager.getPrefab(parentName);

            if (parent == null) {
                logger.error("Missing parent prefab (need to fix parent serialization)");
            } else {
                prefab.addParent(parent);
            }
        }

        for (EntityData.Component componentData : prefabData.getComponentList()) {
            Component component = deserializeComponent(componentData);
            if (component != null) {
                prefab.setComponent(component);
            }
        }

        return prefab;
    }

    @Override
    public Prefab deserializePrefab(EntityData.Prefab prefabData) {
        Prefab prefab = prefabManager.createPrefab(prefabData.getName());
        for (String parentName : prefabData.getParentNameList()) {
            Prefab parent = prefabManager.getPrefab(parentName);
            if (parent == null) {
                logger.error("Missing parent prefab (need to fix parent serialization)");
            } else {
                prefab.addParent(parent);
            }
        }
        for (EntityData.Component componentData : prefabData.getComponentList()) {
            Component component = deserializeComponent(componentData);
            if (component != null) {
                prefab.setComponent(component);
            }
        }
        return prefab;
    }

    @Override
    public Component deserializeComponent(EntityData.Component componentData) {
        Class<? extends Component> componentClass = getComponentClass(componentData);
        if (componentClass != null) {
            ComponentMetadata componentMetadata = componentLibrary.getMetadata(componentClass);
            Component component = componentMetadata.newInstance();
            return deserializeOnto(component, componentData, componentMetadata);
        } else {
            logger.warn("Unable to deserialise unknown component type: {}", componentData.getType());
        }
        return null;
    }

    @Override
    public void setEntityManager(PersistableEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public PersistableEntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public void setPrefabManager(PrefabManager prefabManager) {
        this.prefabManager = prefabManager;
    }

    @Override
    public PrefabManager getPrefabManager() {
        return prefabManager;
    }

    @Override
    public void setComponentLibrary(ComponentLibrary componentLibrary) {
        this.componentLibrary = componentLibrary;
    }

    @Override
    public ComponentLibrary getComponentLibrary() {
        return componentLibrary;
    }

    @Override
    public boolean isUsingLookupTables() {
        return useLookupTables;
    }

    @Override
    public void setUsingLookupTables(boolean enabled) {
        useLookupTables = enabled;
    }

    @Override
    public void setComponentTypeIdTable(Map<Integer, Class<? extends Component>> componentIdTable) {
        this.componentIdTable.clear();
        this.componentIdTable.putAll(componentIdTable);
    }

    @Override
    public void clearComponentTypeIdTable() {
        this.componentIdTable.clear();
    }

    private void writeComponentTypeTable(EntityData.World.Builder world) {
        for (ComponentMetadata<?> componentMetadata : componentLibrary) {
            int index = componentIdTable.size();
            componentIdTable.put(index, componentMetadata.getType());
            world.addComponentClass(ComponentUtil.getComponentClassName(componentMetadata.getType()));
        }
    }

    private void writeIdInfo(final EntityData.World.Builder world, TIntList nonPersistedIds) {
        world.setNextEntityId(entityManager.getNextId());
        entityManager.getFreedIds().forEach(new TIntProcedure() {
            public boolean execute(int i) {
                world.addFreedEntityId(i);
                return true;
            }
        });
        nonPersistedIds.forEach(new TIntProcedure() {
            public boolean execute(int i) {
                world.addFreedEntityId(i);
                return true;
            }
        });
    }

    private Class<? extends Component> getComponentClass(EntityData.Component componentData) {
        if (componentData.hasTypeIndex()) {
            ComponentMetadata metadata = componentLibrary.getMetadata(componentIdTable.get(componentData.getTypeIndex()));
            if (metadata == null) {
                logger.warn("Unable to deserialise unknown component with id: {}", componentData.getTypeIndex());
                return null;
            }
            return metadata.getType();
        } else if (componentData.hasType()) {
            ComponentMetadata metadata = componentLibrary.getMetadata(componentData.getType().toLowerCase(Locale.ENGLISH));
            if (metadata == null) {
                logger.warn("Unable to deserialise unknown component type: {}", componentData.getType());
                return null;
            }
            return metadata.getType();
        }
        logger.warn("Unable to deserialise component, no type provided.");

        return null;
    }

    private EntityData.Entity serializeEntityFull(EntityRef entityRef) {
        EntityData.Entity.Builder entity = EntityData.Entity.newBuilder();
        entity.setId(entityRef.getId());
        for (Component component : entityRef.iterateComponents()) {
            if (component.getClass().equals(EntityInfoComponent.class))
                continue;

            EntityData.Component componentData = serializeComponent(component);
            if (componentData != null) {
                entity.addComponent(componentData);
            }
        }
        return entity.build();
    }

    private EntityData.Entity serializeEntityDelta(EntityRef entityRef, Prefab prefab) {
        EntityData.Entity.Builder entity = EntityData.Entity.newBuilder();
        entity.setId(entityRef.getId());
        entity.setParentPrefab(prefab.getName());
        for (Component component : entityRef.iterateComponents()) {
            if (component.getClass().equals(EntityInfoComponent.class))
                continue;

            Component prefabComponent = prefab.getComponent(component.getClass());
            EntityData.Component componentData;
            if (prefabComponent == null) {
                componentData = serializeComponent(component);
            } else {
                componentData = serializeComponent(prefabComponent, component);
            }

            if (componentData != null) {
                entity.addComponent(componentData);
            }
        }
        for (Component prefabComponent : prefab.listComponents()) {
            if (!entityRef.hasComponent(prefabComponent.getClass())) {
                entity.addRemovedComponent(ComponentUtil.getComponentClassName(prefabComponent.getClass()));
            }
        }
        return entity.build();
    }

    private EntityData.Component serializeComponent(Component base, Component delta) {
        ComponentMetadata<?> componentMetadata = componentLibrary.getMetadata(base.getClass());
        if (componentMetadata == null) {
            logger.error("Unregistered component type: {}", base.getClass());
            return null;
        }

        EntityData.Component.Builder componentMessage = EntityData.Component.newBuilder();
        if (useLookupTables) {
            componentMessage.setTypeIndex(componentIdTable.inverse().get(base.getClass()));
        } else {
            componentMessage.setType(ComponentUtil.getComponentClassName(delta));
        }

        boolean changed = false;
        for (FieldMetadata field : componentMetadata.iterateFields()) {
            try {
                Object origValue = field.getValue(base);
                Object deltaValue = field.getValue(delta);

                if (!Objects.equal(origValue, deltaValue)) {
                    EntityData.Value value = field.serialize(deltaValue);
                    if (value != null) {
                        componentMessage.addField(EntityData.NameValue.newBuilder().setName(field.getName()).setValue(value).build());
                        changed = true;
                    } else {
                        logger.error("Exception serializing component type: {}, field: {} - returned null", base.getClass(), field.getName());
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error("Exception during serializing component type: {}", base.getClass(), e);
            } catch (InvocationTargetException e) {
                logger.error("Exception during serializing component type: {}", base.getClass(), e);
            }
        }
        if (changed) {
            return componentMessage.build();
        }
        return null;
    }

    private Component deserializeComponentOnto(Component component, EntityData.Component componentData) {
        Class<? extends Component> componentClass = getComponentClass(componentData);
        if (componentClass != null) {
            ComponentMetadata componentMetadata = componentLibrary.getMetadata(componentClass);
            return deserializeOnto(component, componentData, componentMetadata);
        } else {
            logger.warn("Unable to deserialise unknown component type: {}", componentData.getType());
        }
        return null;
    }

    private Component deserializeOnto(Component component, EntityData.Component componentData, ComponentMetadata componentMetadata) {
        try {
            for (EntityData.NameValue field : componentData.getFieldList()) {
                FieldMetadata fieldInfo = componentMetadata.getField(field.getName());
                if (fieldInfo == null)
                    continue;

                Object value = fieldInfo.deserialize(field.getValue());
                if (value == null)
                    continue;
                fieldInfo.setValue(component, value);
            }
            return component;
        } catch (InvocationTargetException e) {
            logger.error("Exception during serializing component type: {}", component.getClass(), e);
        } catch (IllegalAccessException e) {
            logger.error("Exception during serializing component type: {}", component.getClass(), e);
        }
        return null;
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
