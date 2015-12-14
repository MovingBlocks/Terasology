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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.persistence.PlayerStore;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 */
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
