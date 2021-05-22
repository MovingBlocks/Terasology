// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network.internal;

import org.terasology.engine.entitySystem.entity.LowLevelEntityManager;
import org.terasology.engine.entitySystem.entity.internal.BaseEntityRef;
import org.terasology.engine.entitySystem.entity.internal.DefaultRefStrategy;
import org.terasology.engine.network.NetworkComponent;

/**
 */
public class NetworkClientRefStrategy extends DefaultRefStrategy {

    private NetworkSystemImpl system;

    public NetworkClientRefStrategy(NetworkSystemImpl system) {
        this.system = system;
    }

    @Override
    public BaseEntityRef createRefFor(long id, LowLevelEntityManager entityManager) {
        NetworkComponent netComp = entityManager.getComponent(id, NetworkComponent.class);
        if (netComp != null && netComp.getNetworkId() != 0) {
            system.registerClientNetworkEntity(netComp.getNetworkId(), id);
            return new NetEntityRef(netComp.getNetworkId(), system, entityManager);
        }
        return super.createRefFor(id, entityManager);
    }
}
