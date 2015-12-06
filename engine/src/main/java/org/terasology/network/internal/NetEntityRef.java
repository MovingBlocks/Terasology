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

package org.terasology.network.internal;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.LowLevelEntityManager;
import org.terasology.entitySystem.entity.internal.BaseEntityRef;
import org.terasology.network.NetworkComponent;

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

    @Override
    public void destroy() {
        super.destroy();
    }
}
