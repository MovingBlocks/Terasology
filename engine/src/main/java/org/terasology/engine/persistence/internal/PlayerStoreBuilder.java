// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import com.google.common.collect.Sets;
import org.joml.Vector3fc;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
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
