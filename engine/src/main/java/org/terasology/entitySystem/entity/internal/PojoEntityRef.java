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
package org.terasology.entitySystem.entity.internal;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.LowLevelEntityManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.network.NetworkComponent;

/**
 */
public class PojoEntityRef extends BaseEntityRef {
    private long id;
    private boolean exists = true;

    PojoEntityRef(LowLevelEntityManager manager, long id) {
        super(manager);
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public EntityRef copy() {
        if (exists) {
            return entityManager.create(entityManager.copyComponents(this).values());
        }
        return NULL;
    }

    @Override
    public boolean exists() {
        return exists;
    }


    @Override
    public String toString() {
        Prefab parent = getParentPrefab();
        StringBuilder builder = new StringBuilder();
        builder.append("EntityRef{id = ");
        builder.append(id);
        NetworkComponent networkComponent = getComponent(NetworkComponent.class);
        if (networkComponent != null) {
            builder.append(", netId = ");
            builder.append(networkComponent.getNetworkId());
        }
        if (parent != null) {
            builder.append(", prefab = '");
            builder.append(parent.getUrn());
            builder.append("'");
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        exists = false;
    }
}
