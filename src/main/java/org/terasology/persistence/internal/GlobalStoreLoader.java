package org.terasology.persistence.internal;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.persistence.serializers.EntitySerializer;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.protobuf.EntityData;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Immortius
 */
public class GlobalStoreLoader {

    private EngineEntityManager entityManager;
    private PrefabManager prefabManager;
    private ComponentLibrary componentLibrary;
    private EntitySerializer entitySerializer;
    private PrefabSerializer prefabSerializer;

    public GlobalStoreLoader(EngineEntityManager entityManager) {
        this.entityManager = entityManager;
        this.prefabManager = entityManager.getPrefabManager();
        this.componentLibrary = entityManager.getComponentLibrary();
        this.entitySerializer = new EntitySerializer(entityManager);
        this.prefabSerializer = new PrefabSerializer(componentLibrary);
    }

    public void load(EntityData.GlobalEntityStore globalStore) {
        entityManager.setNextId(globalStore.getNextEntityId());
        entityManager.getFreedIds().addAll(globalStore.getFreedEntityIdList());

        loadComponentMapping(globalStore);
        loadMissingPrefabs(globalStore);

        for (EntityData.Entity entityData : globalStore.getEntityList()) {
            entitySerializer.deserialize(entityData);
        }

    }

    private void loadMissingPrefabs(EntityData.GlobalEntityStore globalStore) {
        // Prefabs that still need to be created, by their name
        Map<String, EntityData.Prefab> pendingPrefabs = Maps.newHashMap();

        PrefabManager prefabManager = entityManager.getPrefabManager();
        for (EntityData.Prefab prefabData : globalStore.getPrefabList()) {
            if (prefabManager.exists(prefabData.getName())) {
                if (!prefabData.hasParentName()) {
                    createPrefab(prefabData);
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
        Prefab result = prefabManager.getPrefab(prefabData.getName());
        if (result == null) {
            if (prefabManager.getPrefab(prefabData.getParentName()) == null) {
                loadPrefab(pendingPrefabs.get(prefabData.getParentName()), pendingPrefabs);
            }
            result = createPrefab(prefabData);
        }
        return result;
    }

    private Prefab createPrefab(EntityData.Prefab prefabData) {
        PrefabData protoPrefab = prefabSerializer.deserialize(prefabData);
        Prefab prefab = Assets.generateAsset(new AssetUri(AssetType.PREFAB, prefabData.getName()), protoPrefab, Prefab.class);
        prefabManager.registerPrefab(prefab);
        return prefab;
    }

    private void loadComponentMapping(EntityData.GlobalEntityStore globalStore) {
        ComponentLibrary componentLibrary = entityManager.getComponentLibrary();
        Map<Class<? extends Component>, Integer> componentIdTable = Maps.newHashMap();
        for (int index = 0; index < globalStore.getComponentClassCount(); ++index) {
            ClassMetadata componentMetadata = componentLibrary.getMetadata(globalStore.getComponentClass(index));
            if (componentMetadata != null) {
                componentIdTable.put(componentMetadata.getType(), index);
            }
        }

        prefabSerializer.setComponentIdMapping(componentIdTable);
        entitySerializer.setComponentIdMapping(componentIdTable);
    }
}
