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
package org.terasology.persistence.internal;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.persistence.ModuleContext;
import org.terasology.persistence.serializers.EntitySerializer;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.protobuf.EntityData;

import java.util.Map;
import java.util.Optional;

/**
 */
final class GlobalStoreLoader {

    private static final Logger logger = LoggerFactory.getLogger(GlobalStoreLoader.class);

    private ModuleEnvironment environment;
    private EngineEntityManager entityManager;
    private PrefabManager prefabManager;
    private ComponentLibrary componentLibrary;
    private EntitySerializer entitySerializer;
    private PrefabSerializer prefabSerializer;

    public GlobalStoreLoader(ModuleEnvironment environment, EngineEntityManager entityManager, PrefabSerializer prefabSerializer) {
        this.entityManager = entityManager;
        this.prefabManager = entityManager.getPrefabManager();
        this.environment = environment;
        this.componentLibrary = entityManager.getComponentLibrary();
        this.entitySerializer = new EntitySerializer(entityManager);
        this.prefabSerializer = prefabSerializer;
    }

    public void load(EntityData.GlobalStore globalStore) {
        entityManager.clear();
        entityManager.setNextId(globalStore.getNextEntityId());

        loadComponentMapping(globalStore);
        loadMissingPrefabs(globalStore);

        for (EntityData.Entity entityData : globalStore.getEntityList()) {
            entitySerializer.deserialize(entityData);
        }
    }

    private void loadMissingPrefabs(EntityData.GlobalStore globalStore) {
        // Prefabs that still need to be created, by their name
        Map<String, EntityData.Prefab> pendingPrefabs = Maps.newHashMap();
        for (EntityData.Prefab prefabData : globalStore.getPrefabList()) {
            if (!prefabManager.exists(prefabData.getName())) {
                if (!prefabData.hasParentName()) {
                    Module module = environment.get(new SimpleUri(prefabData.getName()).getModuleName());
                    try (ModuleContext.ContextSpan ignored = ModuleContext.setContext(module)) {
                        createPrefab(prefabData);
                    } catch (Exception e) {
                        logger.error("Failed to load prefab {}", prefabData.getName(), e);
                    }
                } else {
                    pendingPrefabs.put(prefabData.getName(), prefabData);
                }
            }
        }

        for (EntityData.Prefab prefabData : pendingPrefabs.values()) {
            loadPrefab(prefabData, pendingPrefabs);
        }
    }

    private Prefab loadPrefab(EntityData.Prefab prefabData, Map<String, EntityData.Prefab> pendingPrefabs) {
        Optional<Prefab> result = Assets.getPrefab(prefabData.getName());
        if (!result.isPresent()) {
            if (prefabData.hasParentName() && pendingPrefabs.containsKey(prefabData.getParentName())) {
                loadPrefab(pendingPrefabs.get(prefabData.getParentName()), pendingPrefabs);
            }
            Module module = environment.get(new SimpleUri(prefabData.getName()).getModuleName());
            try (ModuleContext.ContextSpan ignored = ModuleContext.setContext(module)) {
                return createPrefab(prefabData);
            } catch (Exception e) {
                logger.error("Failed to load prefab {}", prefabData.getParentName(), e);
                return null;
            }
        }
        return result.get();
    }

    private Prefab createPrefab(EntityData.Prefab prefabData) {
        PrefabData protoPrefab = prefabSerializer.deserialize(prefabData);
        return Assets.generateAsset(new ResourceUrn(prefabData.getName()), protoPrefab, Prefab.class);
    }

    private void loadComponentMapping(EntityData.GlobalStore globalStore) {
        Map<Class<? extends Component>, Integer> componentIdTable = Maps.newHashMap();
        for (int index = 0; index < globalStore.getComponentClassCount(); ++index) {
            ComponentMetadata<?> componentMetadata = componentLibrary.resolve(globalStore.getComponentClass(index));
            if (componentMetadata != null) {
                componentIdTable.put(componentMetadata.getType(), index);
            } else {
                logger.warn("Unable to resolve component '{}'", globalStore.getComponentClass(index));
            }
        }

        prefabSerializer.setComponentIdMapping(componentIdTable);
        entitySerializer.setComponentIdMapping(componentIdTable);
    }
}
