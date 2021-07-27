// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.entity.internal;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.LowLevelEntityManager;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.network.NetworkComponent;

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
