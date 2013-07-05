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

import org.terasology.entitySystem.EntityRef;
import org.terasology.persistence.PlayerStore;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class PlayerStoreInternal implements PlayerStore {

    private String id;
    private StorageManagerInternal manager;

    PlayerStoreInternal(String id, StorageManagerInternal entityStoreManager) {
        this.id = id;
        this.manager = entityStoreManager;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void save() {
        manager.store(this);
    }

    @Override
    public void storeCharacter(EntityRef character) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EntityRef restoreCharacter() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setRelevanceLocation(Vector3f location) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Vector3f getRelevanceLocation() {
        return new Vector3f();
    }

    @Override
    public boolean hasCharacter() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
