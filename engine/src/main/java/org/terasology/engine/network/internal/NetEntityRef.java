// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.LowLevelEntityManager;
import org.terasology.engine.entitySystem.entity.internal.BaseEntityRef;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

/**
 * An implementation of EntityRef that deals with entities propagated to a client. These entities may enter and
 * leave relevance over time, and may have a different Entity id each time. NetEntityRef links to them via their
 * network id, and survives them dropping in and out of relevance.
 *
 */
public class NetEntityRef extends BaseEntityRef {

    private final int networkId;
    private NetworkSystemImpl networkSystem;
    private boolean exists = true;

    public NetEntityRef(int networkId, NetworkSystemImpl system, LowLevelEntityManager entityManager) {
        super(entityManager);
        this.networkId = networkId;
        this.networkSystem = system;
    }

    public int getNetworkId() {
        return networkId;
    }

    @Override
    public EntityRef copy() {
        if (!isActive()) {
            return NULL;
        }
        Map<Class<? extends Component>, Component> classComponentMap = entityManager.copyComponents(this);
        if (networkSystem.getMode().isAuthority()) {
            NetworkComponent netComp = (NetworkComponent) classComponentMap.get(NetworkComponent.class);
            if (netComp != null) {
                netComp.setNetworkId(0);
            }
        } else {
            classComponentMap.remove(NetworkComponent.class);
        }
        return entityManager.create(classComponentMap.values());
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public long getId() {
        return networkSystem.getEntityId(networkId);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        exists = false;
    }
}
