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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityDestroySubscriber;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.PlayerStore;
import org.terasology.protobuf.EntityData;

import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public class StorageManagerInternal implements StorageManager, EntityDestroySubscriber {

    private EngineEntityManager entityManager;
    private Map<String, EntityData.PlayerEntityStore> playerStores = Maps.newHashMap();
    private TIntObjectMap<List<StoreRefTable>> externalRefHolderLookup = new TIntObjectHashMap<>();
    private Map<String, StoreRefTable> playerStoreExternalRefs = Maps.newHashMap();

    public StorageManagerInternal(EngineEntityManager entityManager) {
        this.entityManager = entityManager;
        entityManager.subscribe(this);
    }

    @Override
    public PlayerStore createPlayerStoreForSave(String playerId) {
        return new PlayerStoreInternal(playerId, this, entityManager);
    }

    @Override
    public void flush() {
    }

    @Override
    public PlayerStore loadStore(String playerId) {
        EntityData.PlayerEntityStore store = playerStores.get(playerId);
        if (store != null) {
            TIntSet validRefs = null;
            StoreRefTable table = playerStoreExternalRefs.get(playerId);
            if (table != null) {
                validRefs = table.externalReferences;
            }
            return new PlayerStoreInternal(playerId, store, validRefs, this, entityManager);
        }
        return null;
    }

    public void store(String id, EntityData.PlayerEntityStore playerStore, TIntSet externalReference) {
        if (externalReference.size() > 0) {
            StoreRefTable refTable = new StoreRefTable(id, externalReference);
            playerStoreExternalRefs.put(id, refTable);
            TIntIterator iterator = externalReference.iterator();
            while (iterator.hasNext()) {
                int refId = iterator.next();
                List<StoreRefTable> tables = externalRefHolderLookup.get(refId);
                if (tables == null) {
                    tables = Lists.newArrayList();
                    externalRefHolderLookup.put(refId, tables);
                }
                tables.add(refTable);
            }
        }
        playerStores.put(id, playerStore);
    }

    @Override
    public void onEntityDestroyed(int entityId) {
        List<StoreRefTable> tables = externalRefHolderLookup.remove(entityId);
        for (StoreRefTable table : tables) {
            table.externalReferences.remove(entityId);
            if (table.externalReferences.isEmpty()) {
                playerStoreExternalRefs.remove(table.id);
            }
        }
    }

    private static class StoreRefTable {
        private String id;
        private TIntSet externalReferences;

        public StoreRefTable(String id, TIntSet externalReferences) {
            this.id = id;
            this.externalReferences = externalReferences;
        }
    }
}
