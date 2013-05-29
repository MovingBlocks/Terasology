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

package org.terasology.entitySystem.persistence;

import com.google.common.collect.Maps;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EntityStore;
import org.terasology.entitySystem.lifecycleEvents.OnStoreEvent;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.protobuf.EntityData;
import org.terasology.world.chunks.Chunk;

import javax.vecmath.Vector3f;
import java.io.*;
import java.util.Map;

/**
 *
 */
public class PlayerEntityStore implements EntityStore {
    private static final String PLAYER_STORE_SUBDIR = "players";

    private EngineEntityManager entityManager;
    private File playerDataFile;
    private EntitySerializer serializer;
    private Vector3f relevanceLocation = new Vector3f(Chunk.SIZE_X / 2, Chunk.SIZE_Y / 2, Chunk.SIZE_Z / 2);
    private boolean hasCharacter = false;

    private EntityData.EntityStore.Builder entityStoreBuilder;

    private EntityData.EntityStore loadedData;


    public PlayerEntityStore(String playerId, EngineEntityManager manager) {
        entityManager = manager;
        serializer = new EntitySerializer(manager, manager.getComponentLibrary());
        serializer.setIgnoringEntityId(false);
        File playerSubDir = new File(PathManager.getInstance().getCurrentWorldPath(), PLAYER_STORE_SUBDIR);
        playerSubDir.mkdirs();
        playerDataFile = new File(playerSubDir, playerId + ".dat");
    }

    public void beginStore() {
        entityStoreBuilder = EntityData.EntityStore.newBuilder();
        setSerializerComponentIds();
    }

    public void setRelevanceLocation(Vector3f location) {
        this.relevanceLocation.set(location);
    }

    public Vector3f getRelevanceLocation() {
        return new Vector3f(relevanceLocation);
    }

    public void setHasCharacter(boolean hasCharacter) {
        this.hasCharacter = hasCharacter;
    }

    public boolean hasCharacter() {
        return hasCharacter;
    }

    @Override
    public void store(EntityRef entity) {
        store(entity, "");
    }

    @Override
    public void store(EntityRef entity, String name) {
        if (entityStoreBuilder == null) {
            throw new IllegalStateException("Entity Store not ready for storing entities");
        }
        if (entity.exists()) {
            entity.send(new OnStoreEvent(this));
            EntityData.Entity entityData = serializer.serialize(entity, true, FieldSerializeCheck.NullCheck.<Component>newInstance());
            entityStoreBuilder.addEntity(entityData);
            entityStoreBuilder.addEntityName(name);
            entityManager.removedForStoring(entity);
        }
    }

    public void endStore() throws IOException {

        try (FileOutputStream out = new FileOutputStream(playerDataFile)) {
            BufferedOutputStream bos = new BufferedOutputStream(out);
            EntityData.PlayerEntityStore.Builder playerEntityStore = EntityData.PlayerEntityStore.newBuilder();
            playerEntityStore.setStore(entityStoreBuilder);
            playerEntityStore.setCharacterPosX(relevanceLocation.x);
            playerEntityStore.setCharacterPosY(relevanceLocation.y);
            playerEntityStore.setCharacterPosZ(relevanceLocation.z);
            playerEntityStore.setHasCharacter(hasCharacter);
            playerEntityStore.build().writeTo(bos);
            bos.flush();
        }
        entityStoreBuilder = null;
    }

    public void beginRestore() throws IOException {
        if (playerDataFile.exists()) {
            try (InputStream in = new FileInputStream(playerDataFile)) {
                EntityData.PlayerEntityStore playerEntityStore = EntityData.PlayerEntityStore.parseFrom(in);
                loadedData = playerEntityStore.getStore();
                if (playerEntityStore.hasCharacterPosX() && playerEntityStore.hasCharacterPosY() && playerEntityStore.hasCharacterPosZ()) {
                    relevanceLocation.set(playerEntityStore.getCharacterPosX(), playerEntityStore.getCharacterPosY(), playerEntityStore.getCharacterPosZ());
                }
                if (playerEntityStore.hasHasCharacter()) {
                    hasCharacter = playerEntityStore.getHasCharacter();
                }
            } catch (IOException e) {
                loadedData = EntityData.EntityStore.newBuilder().build();
                throw new IOException(e);
            }
        } else {
            loadedData = EntityData.EntityStore.newBuilder().build();
        }
    }

    @Override
    public Map<String, EntityRef> restoreAll()  {
        if (loadedData == null) {
            throw new IllegalStateException("Entity store not ready to restore entities");
        }
        Map<String, EntityRef> namedEntities = Maps.newHashMap();
        Map<Class<? extends Component>, Integer> idMap = Maps.newHashMap();
        for (int i = 0; i < loadedData.getComponentClassCount(); ++i) {
            ClassMetadata<? extends Component> metadata = entityManager.getComponentLibrary().getMetadata(loadedData.getComponentClass(i));
            if (metadata != null) {
                idMap.put(metadata.getType(), i);
            }
        }
        serializer.setComponentIdMapping(idMap);
        for (int i = 0; i < loadedData.getEntityCount(); ++i) {
            EntityRef entity = serializer.deserialize(loadedData.getEntity(i));
            if (!loadedData.getEntityName(i).isEmpty()) {
                namedEntities.put(loadedData.getEntityName(i), entity);
            }
        }
        loadedData = null;
        return namedEntities;
    }

    private void setSerializerComponentIds() {
        Map<Class<? extends Component>, Integer> componentIds = Maps.newHashMap();
        for (ClassMetadata<? extends Component> componentMetadata : entityManager.getComponentLibrary()) {
            entityStoreBuilder.addComponentClass(componentMetadata.getName());
            componentIds.put(componentMetadata.getType(), componentIds.size());
        }
        serializer.setComponentIdMapping(componentIds);
    }


}
