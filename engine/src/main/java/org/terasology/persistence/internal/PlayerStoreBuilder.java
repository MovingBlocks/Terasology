/*
 * Copyright 2014 MovingBlocks
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

import com.google.common.collect.Sets;
import org.joml.Vector3fc;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.math.geom.Vector3f;
import org.terasology.protobuf.EntityData;

import java.util.Set;

/**
 *
 * Has all the data of a player to create save it with a copy of the entity manager.
 *
 */
class PlayerStoreBuilder {
    private Long characterEntityId;
    private Vector3fc relevanceLocation;
    private Set<EntityRef> storedEntities;

     PlayerStoreBuilder(Long characterEntityId, Vector3fc relevanceLocation) {
        this.characterEntityId = characterEntityId;
        this.relevanceLocation = relevanceLocation;
    }

    public EntityData.PlayerStore build(EngineEntityManager entityManager) {
        EntityData.PlayerStore.Builder playerEntityStore = EntityData.PlayerStore.newBuilder();
        playerEntityStore.setCharacterPosX(relevanceLocation.x());
        playerEntityStore.setCharacterPosY(relevanceLocation.y());
        playerEntityStore.setCharacterPosZ(relevanceLocation.z());
        playerEntityStore.setHasCharacter(characterEntityId != null);
        if (characterEntityId != null) {
            EntityRef character = entityManager.getEntity(characterEntityId);
            EntityStorer store = new EntityStorer(entityManager);
            store.store(character, PlayerStoreInternal.CHARACTER);
            storedEntities = store.getStoredEntities();
            playerEntityStore.setStore(store.finaliseStore());
        } else {
            storedEntities = Sets.newHashSet();
        }
        return playerEntityStore.build();
    }

    public Long getCharacterEntityId() {
        return characterEntityId;
    }

    /**
     *
     * @return all entitites that got stored when {@link #build(EngineEntityManager)} got called.
     */
    public Set<EntityRef> getStoredEntities() {
        return storedEntities;
    }
}
