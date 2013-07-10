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
final class GlobalStoreSaver {

    private EngineEntityManager entityManager;
    private EntityData.GlobalStore.Builder store;
    private EntitySerializer entitySerializer;

    private TIntSet nonPersistentIds = new TIntHashSet();

    public GlobalStoreSaver(EngineEntityManager entityManager) {
        this.entityManager = entityManager;
        this.store = EntityData.GlobalStore.newBuilder();
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

    public void addStoreMetadata(StoreMetadata metadata) {
        EntityData.EntityStoreMetadata.Builder referenceSet = EntityData.EntityStoreMetadata.newBuilder();
        TIntIterator iterator = metadata.getExternalReferences().iterator();
        while (iterator.hasNext()) {
            int ref = iterator.next();
            if (!nonPersistentIds.contains(ref)) {
                referenceSet.addReference(ref);
            }
        }
        if (referenceSet.getReferenceCount() > 0) {
            metadata.getId().setUpIdentity(referenceSet);
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

    public EntityData.GlobalStore save() {
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
