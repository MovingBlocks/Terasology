// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.persistence.PlayerStore;
import org.terasology.protobuf.EntityData;

import java.util.Map;

final class PlayerStoreInternal implements PlayerStore {
    static final String CHARACTER = "character";

    private final EngineEntityManager entityManager;
    private final String id;
    private final Vector3f relevanceLocation = new Vector3f();
    private EntityRef character = EntityRef.NULL;
    private boolean hasCharacter;
    private EntityData.EntityStore entityStore;

    PlayerStoreInternal(String id, EngineEntityManager entityManager) {
        this.id = id;
        this.entityManager = entityManager;
    }

    PlayerStoreInternal(String id, EntityData.PlayerStore store, EngineEntityManager entityManager) {
        this.id = id;
        this.entityManager = entityManager;
        this.entityStore = store.getStore();
        this.relevanceLocation.set(store.getCharacterPosX(), store.getCharacterPosY(), store.getCharacterPosZ());
        this.hasCharacter = store.getHasCharacter();
    }

    @Override
    public String getId() {
        return id;
    }


    @Override
    public void restoreEntities() {
        if (entityStore != null) {
            EntityRestorer restorer = new EntityRestorer(entityManager);
            Map<String, EntityRef> refMap = restorer.restore(entityStore);
            EntityRef loadedCharacter = refMap.get(CHARACTER);
            if (loadedCharacter != null) {
                setCharacter(loadedCharacter);
            }
            entityStore = null;
        }
    }

    @Override
    public void setCharacter(EntityRef character) {
        this.character = character;
        hasCharacter = character.exists();
        LocationComponent location = character.getComponent(LocationComponent.class);
        if (location == null) {
            return;
        }

        Vector3f position = location.getWorldPosition(new Vector3f());
        if (position.isFinite()) {
            setRelevanceLocation(position);
        }
    }

    @Override
    public EntityRef getCharacter() {
        return character;
    }

    @Override
    public void setRelevanceLocation(Vector3fc location) {
        relevanceLocation.set(location);
    }

    @Override
    public Vector3fc getRelevanceLocation() {
        return relevanceLocation;
    }

    @Override
    public boolean hasCharacter() {
        return hasCharacter;
    }
}
