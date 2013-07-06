package org.terasology.persistence.internal;

import com.google.common.collect.Maps;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.persistence.serializers.EntitySerializer;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * @author Immortius
 */
class GlobalEntityStore {

    private EngineEntityManager entityManager;
    private EntityData.GlobalEntityStore.Builder store;
    private EntitySerializer entitySerializer;

    private TIntSet nonPersistentIds = new TIntHashSet();

    public GlobalEntityStore(EngineEntityManager entityManager) {
        this.entityManager = entityManager;
        this.store = EntityData.GlobalEntityStore.newBuilder();
        this.entitySerializer = new EntitySerializer(entityManager);

        Map<Class<? extends Component>, Integer> componentIdTable = Maps.newHashMap();
        for (ClassMetadata<? extends Component> componentMetadata : entityManager.getComponentLibrary()) {
            store.addComponentClass(componentMetadata.getName());
            componentIdTable.put(componentMetadata.getType(), componentIdTable.size());
        }
        entitySerializer.setComponentIdMapping(componentIdTable);

        PrefabSerializer prefabSerializer = new PrefabSerializer(entityManager.getComponentLibrary());
        prefabSerializer.setComponentIdMapping(componentIdTable);
        for (Prefab prefab : entityManager.getPrefabManager().listPrefabs()) {
            store.addPrefab(prefabSerializer.serialize(prefab));
        }
    }

    public void addRefTable(String id, TIntSet references) {
        EntityData.EntityStoreReferenceSet.Builder referenceSet = EntityData.EntityStoreReferenceSet.newBuilder();
        TIntIterator iterator = references.iterator();
        while (iterator.hasNext()) {
            int ref = iterator.next();
            if (!nonPersistentIds.contains(ref)) {
                referenceSet.addReference(ref);
            }
        }
        if (referenceSet.getReferenceCount() > 0) {
            referenceSet.setStoreId(id);
            store.addStoreReferenceSet(referenceSet);
        }
    }

    public void store(EntityRef entity) {
        if (entity.isPersistent()) {
            store.addEntity(entitySerializer.serialize(entity));
        } else {
            nonPersistentIds.add(entity.getId());
        }
    }

    public EntityData.GlobalEntityStore save() {
        writeIdInfo();

        return store.build();
    }

    private void writeIdInfo() {
        store.setNextEntityId(entityManager.getNextId());
        entityManager.getFreedIds().forEach(new TIntProcedure() {
            public boolean execute(int i) {
                store.addFreedEntityId(i);
                return true;
            }
        });
        nonPersistentIds.forEach(new TIntProcedure() {
            public boolean execute(int i) {
                store.addFreedEntityId(i);
                return true;
            }
        });
    }
}
