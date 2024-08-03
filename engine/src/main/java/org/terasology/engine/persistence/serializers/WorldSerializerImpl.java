// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.serializers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.module.ModuleContext;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabData;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.protobuf.EntityData;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of WorldSerializer for EngineEntityManager.
 *
 */
public class WorldSerializerImpl implements WorldSerializer {

    private static final Logger logger = LoggerFactory.getLogger(WorldSerializerImpl.class);

    private ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
    private ComponentLibrary componentLibrary;
    private PrefabManager prefabManager;
    private EngineEntityManager entityManager;
    private EntitySerializer entitySerializer;
    private PrefabSerializer prefabSerializer;

    public WorldSerializerImpl(EngineEntityManager entityManager, PrefabSerializer prefabSerializer) {
        this.entityManager = entityManager;
        this.prefabManager = entityManager.getPrefabManager();
        this.componentLibrary = entityManager.getComponentLibrary();
        this.entitySerializer = new EntitySerializer(entityManager);
        this.prefabSerializer = prefabSerializer;
    }

    @Override
    public EntityData.GlobalStore serializeWorld(boolean verbose) {
        final EntityData.GlobalStore.Builder world = EntityData.GlobalStore.newBuilder();

        if (!verbose) {
            writeComponentTypeTable(world);
        }

        for (Prefab prefab : prefabManager.listPrefabs()) {
            world.addPrefab(prefabSerializer.serialize(prefab));
        }

        for (EntityRef entity : entityManager.getAllEntities()) {
            if (verbose || entity.isPersistent()) {
                world.addEntity(entitySerializer.serialize(entity));
            }
        }

        writeIdInfo(world);

        entitySerializer.removeComponentIdMapping();
        prefabSerializer.removeComponentIdMapping();
        return world.build();
    }

    @Override
    public EntityData.GlobalStore serializeWorld(boolean verbose, List<Class<? extends Component>> filterComponents) {
        if (filterComponents == null) {
            return serializeWorld(true);
        }
        final EntityData.GlobalStore.Builder world = EntityData.GlobalStore.newBuilder();

        if (!verbose) {
            writeComponentTypeTable(world);
        }

        for (Prefab prefab : prefabManager.listPrefabs()) {
            if (prefab.hasAnyComponents(filterComponents)) {
                world.addPrefab(prefabSerializer.serialize(prefab));
            }
        }

        for (EntityRef entity : entityManager.getAllEntities()) {
            if ((verbose || entity.isPersistent()) && entity.hasAnyComponents(filterComponents)) {
                world.addEntity(entitySerializer.serialize(entity));
            }
        }

        writeIdInfo(world);

        entitySerializer.removeComponentIdMapping();
        prefabSerializer.removeComponentIdMapping();
        return world.build();
    }

    @Override
    public void deserializeWorld(EntityData.GlobalStore world) {
        entityManager.setNextId(world.getNextEntityId());

        Map<Class<? extends Component>, Integer> componentIdTable = Maps.newHashMap();
        for (int index = 0; index < world.getComponentClassCount(); ++index) {
            ComponentMetadata<?> componentMetadata = componentLibrary.resolve(world.getComponentClass(index));
            if (componentMetadata != null) {
                componentIdTable.put(componentMetadata.getType(), index);
            }
        }
        entitySerializer.setComponentIdMapping(componentIdTable);
        prefabSerializer.setComponentIdMapping(componentIdTable);

        // Prefabs that still need to be created, by their required parent
        ListMultimap<String, EntityData.Prefab> pendingPrefabs = ArrayListMultimap.create();

        world.getPrefabList().stream().filter(prefabData -> !prefabManager.exists(prefabData.getName())).forEach(prefabData -> {
            if (!prefabData.hasParentName()) {
                createPrefab(prefabData);
            } else {
                pendingPrefabs.put(prefabData.getParentName(), prefabData);
            }
        });

        while (!pendingPrefabs.isEmpty()) {
            Iterator<Map.Entry<String, Collection<EntityData.Prefab>>> i = pendingPrefabs.asMap().entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, Collection<EntityData.Prefab>> entry = i.next();
                if (prefabManager.exists(entry.getKey())) {
                    entry.getValue().forEach(this::createPrefab);
                    i.remove();
                }
            }
        }

        for (EntityData.Entity entityData : world.getEntityList()) {
            entitySerializer.deserialize(entityData);
        }

        entitySerializer.removeComponentIdMapping();
        prefabSerializer.removeComponentIdMapping();
    }


    private void createPrefab(EntityData.Prefab prefabData) {
        SimpleUri uri = new SimpleUri(prefabData.getName());
        try (ModuleContext.ContextSpan ignored = ModuleContext.setContext(moduleManager.getEnvironment().get(uri.getModuleName()))) {
            PrefabData protoPrefab = prefabSerializer.deserialize(prefabData);
            Assets.generateAsset(new ResourceUrn(prefabData.getName()), protoPrefab, Prefab.class);
        } catch (Exception e) {
            logger.error("Failed to create prefab {}", prefabData.getName()); //NOPMD
        }

    }

    private void writeComponentTypeTable(EntityData.GlobalStore.Builder world) {
        Map<Class<? extends Component>, Integer> componentIdTable = Maps.newHashMap();
        for (ComponentMetadata<?> componentMetadata : componentLibrary.iterateComponentMetadata()) {
            int index = componentIdTable.size();
            componentIdTable.put(componentMetadata.getType(), index);
            world.addComponentClass(componentMetadata.getId().toString());
        }
        entitySerializer.setComponentIdMapping(componentIdTable);
        prefabSerializer.setComponentIdMapping(componentIdTable);
    }

    private void writeIdInfo(final EntityData.GlobalStore.Builder world) {
        world.setNextEntityId(entityManager.getNextId());
    }

}
