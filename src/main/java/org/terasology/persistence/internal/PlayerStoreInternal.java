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
package org.terasology.persistence.internal;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.persistence.PlayerStore;
import org.terasology.protobuf.EntityData;

import javax.vecmath.Vector3f;
import java.util.Map;

/**
 * @author Immortius
 */
final class PlayerStoreInternal implements PlayerStore {
    private static final String CHARACTER = "character";

    private final EngineEntityManager entityManager;
    private final String id;
    private final StorageManagerInternal manager;
    private final Vector3f relevanceLocation = new Vector3f();
    private EntityRef character = EntityRef.NULL;
    private boolean hasCharacter = false;
    private EntityData.EntityStore entityStore;
    private TIntSet externalRefs;

    PlayerStoreInternal(String id, StorageManagerInternal entityStoreManager, EngineEntityManager entityManager) {
        this.id = id;
        this.manager = entityStoreManager;
        this.entityManager = entityManager;
    }

    PlayerStoreInternal(String id, EntityData.PlayerStore store, TIntSet externalRefs, StorageManagerInternal entityStoreManager, EngineEntityManager entityManager) {
        this.id = id;
        this.manager = entityStoreManager;
        this.entityManager = entityManager;
        this.entityStore = store.getStore();
        this.relevanceLocation.set(store.getCharacterPosX(), store.getCharacterPosY(), store.getCharacterPosZ());
        this.hasCharacter = store.getHasCharacter();
        this.externalRefs = externalRefs;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void save() {
        EntityData.PlayerStore.Builder playerEntityStore = EntityData.PlayerStore.newBuilder();
        playerEntityStore.setCharacterPosX(relevanceLocation.x);
        playerEntityStore.setCharacterPosY(relevanceLocation.y);
        playerEntityStore.setCharacterPosZ(relevanceLocation.z);
        playerEntityStore.setHasCharacter(hasCharacter());
        EntityStorer storer = new EntityStorer(entityManager);
        storer.store(character, CHARACTER);
        playerEntityStore.setStore(storer.finaliseStore());
        manager.store(id, playerEntityStore.build(), storer.getExternalReferences());
    }

    @Override
    public void restore() {
        if (entityStore != null) {
            EntityRestorer restorer = new EntityRestorer(entityManager);
            Map<String, EntityRef> refMap = restorer.restore(entityStore, externalRefs);
            EntityRef character = refMap.get(CHARACTER);
            if (character != null) {
                this.character = character;
            }
            entityStore = null;
        }
    }

    @Override
    public void setCharacter(EntityRef character) {
        this.character = character;
        hasCharacter = character.exists();
        LocationComponent location = character.getComponent(LocationComponent.class);
        if (location != null) {
            setRelevanceLocation(location.getWorldPosition());
        }
    }

    @Override
    public EntityRef getCharacter() {
        return character;
    }

    @Override
    public void setRelevanceLocation(Vector3f location) {
        relevanceLocation.set(location);
    }

    @Override
    public Vector3f getRelevanceLocation() {
        return relevanceLocation;
    }

    @Override
    public boolean hasCharacter() {
        return hasCharacter;
    }
}
