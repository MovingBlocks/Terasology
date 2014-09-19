/*
 * Copyright 2013 MovingBlocks
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.persistence.ModuleContext;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.CoreRegistry;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Implementation of WorldSerializer for EngineEntityManager.
 *
 * @author Immortius <immortius@gmail.com>
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

        TIntList nonPersistedIds = new TIntArrayList();
        for (EntityRef entity : entityManager.getAllEntities()) {
            if (verbose || entity.isPersistent()) {
                world.addEntity(entitySerializer.serialize(entity));
            } else {
                nonPersistedIds.add(entity.getId());
            }
        }

        writeIdInfo(world, nonPersistedIds);

        entitySerializer.removeComponentIdMapping();
        prefabSerializer.removeComponentIdMapping();
        return world.build();
    }


    @Override
    public void deserializeWorld(EntityData.GlobalStore world) {
        entityManager.setNextId(world.getNextEntityId());
        for (Integer deadId : world.getFreedEntityIdList()) {
            entityManager.getFreedIds().add(deadId);
        }

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

        for (EntityData.Prefab prefabData : world.getPrefabList()) {
            if (!prefabManager.exists(prefabData.getName())) {
                if (!prefabData.hasParentName()) {
                    createPrefab(prefabData);
                } else {
                    pendingPrefabs.put(prefabData.getParentName(), prefabData);
                }
            }
        }

        while (!pendingPrefabs.isEmpty()) {
            Iterator<Map.Entry<String, Collection<EntityData.Prefab>>> i = pendingPrefabs.asMap().entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, Collection<EntityData.Prefab>> entry = i.next();
                if (prefabManager.exists(entry.getKey())) {
                    for (EntityData.Prefab prefabData : entry.getValue()) {
                        createPrefab(prefabData);
                    }
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
            Assets.generateAsset(new AssetUri(AssetType.PREFAB, prefabData.getName()), protoPrefab, Prefab.class);
        } catch (Exception e) {
            logger.error("Failed to create prefab {}", prefabData.getName());
        }

    }

    private void writeComponentTypeTable(EntityData.GlobalStore.Builder world) {
        Map<Class<? extends Component>, Integer> componentIdTable = Maps.newHashMap();
        for (ComponentMetadata<?> componentMetadata : componentLibrary.iterateComponentMetadata()) {
            int index = componentIdTable.size();
            componentIdTable.put(componentMetadata.getType(), index);
            world.addComponentClass(componentMetadata.getUri().toString());
        }
        entitySerializer.setComponentIdMapping(componentIdTable);
        prefabSerializer.setComponentIdMapping(componentIdTable);
    }

    private void writeIdInfo(final EntityData.GlobalStore.Builder world, TIntList nonPersistedIds) {
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

}
