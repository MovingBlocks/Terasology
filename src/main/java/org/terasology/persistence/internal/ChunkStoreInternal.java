package org.terasology.persistence.internal;

import com.google.common.collect.Lists;
import gnu.trove.set.TIntSet;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;
import org.terasology.persistence.ChunkStore;
import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.EntityData;
import org.terasology.world.chunks.Chunk;

import java.util.List;

/**
 * @author Immortius
 */
final class ChunkStoreInternal implements ChunkStore {

    private StorageManagerInternal storageManager;
    private Vector3i chunkPosition;
    private Chunk chunk;

    private EngineEntityManager entityManager;
    private EntityData.EntityStore entityStore;
    private TIntSet externalRefs;
    private List<EntityRef> entitiesToStore = Lists.newArrayList();

    public ChunkStoreInternal(Chunk chunk, StorageManagerInternal storageManager, EngineEntityManager entityManager) {
        this.chunk = chunk;
        this.chunkPosition = new Vector3i(chunk.getPos());
        this.storageManager = storageManager;
        this.entityManager = entityManager;
    }

    public ChunkStoreInternal(EntityData.ChunkStore chunkData, TIntSet externalRefs, StorageManagerInternal storageManager, EngineEntityManager entityManager) {
        this.chunkPosition = new Vector3i(chunkData.getX(), chunkData.getY(), chunkData.getZ());
        this.storageManager = storageManager;
        this.entityManager = entityManager;
        ChunksProtobuf.Chunk chunkDataForDecode = ChunksProtobuf.Chunk.newBuilder().setX(chunkData.getX()).setY(chunkData.getY()).setZ(chunkData.getZ()).setBlockData(chunkData.getBlockData()).setSunlightData(chunkData.getSunlightData()).setLightData(chunkData.getLightData()).setExtraData(chunkData.getLiquidData()).setState(ChunksProtobuf.State.COMPLETE).build();
        this.chunk = new Chunk.ProtobufHandler().decode(chunkDataForDecode);
        this.entityStore = chunkData.getStore();
        this.externalRefs = externalRefs;
    }

    @Override
    public Vector3i getChunkPosition() {
        return new Vector3i(chunkPosition);
    }

    @Override
    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public void save() {
        save(true);
    }

    @Override
    public void save(boolean deactivateEntities) {
        EntityStorer storer = new EntityStorer(entityManager);
        for (EntityRef entityRef : entitiesToStore) {
            storer.store(entityRef, deactivateEntities);
        }
        entityStore = storer.finaliseStore();
        externalRefs = storer.getExternalReferences();
        storageManager.store(this, externalRefs);
        entitiesToStore.clear();
    }

    @Override
    public void store(EntityRef entity) {
        entitiesToStore.add(entity);
    }

    @Override
    public void storeAllEntities() {
        for (EntityRef entity : entityManager.getEntitiesWith(LocationComponent.class)) {

        }
    }

    @Override
    public void restoreEntities() {
        new EntityRestorer(entityManager).restore(entityStore, externalRefs);
    }

    public EntityData.ChunkStore getStore() {
        EntityData.ChunkStore.Builder builder = EntityData.ChunkStore.newBuilder();
        builder.setX(chunkPosition.x);
        builder.setY(chunkPosition.y);
        builder.setZ(chunkPosition.z);

        chunk.lock();
        try {
            ChunksProtobuf.Chunk encoded = new Chunk.ProtobufHandler().encode(chunk, false);
            builder.setBlockData(encoded.getBlockData());
            builder.setSunlightData(encoded.getSunlightData());
            builder.setLightData(encoded.getLightData());
            builder.setLiquidData(encoded.getExtraData());
            builder.setStore(entityStore);
        } finally {
            chunk.unlock();
        }
        return builder.build();
    }
}
