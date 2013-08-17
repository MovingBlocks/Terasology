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

import org.terasology.entitySystem.EntityRef;
import org.terasology.persistence.GlobalStore;
import org.terasology.protobuf.EntityData;

/**
 * @author Immortius
 */
class GlobalStoreInternal implements GlobalStore {

    private GlobalStoreSaver globalStoreSaver;
    private StorageManagerInternal storageManager;

    public GlobalStoreInternal(GlobalStoreSaver globalStoreSave, StorageManagerInternal storageManager) {
        this.globalStoreSaver = globalStoreSave;
        this.storageManager = storageManager;
    }

    @Override
    public void store(EntityRef entity) {
        globalStoreSaver.store(entity);
    }

    @Override
    public void save() {
        EntityData.GlobalStore globalStoreData = globalStoreSaver.save();
        storageManager.store(globalStoreData);
    }
}
