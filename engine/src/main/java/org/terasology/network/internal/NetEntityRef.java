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

import org.terasology.entitySystem.entity.LowLevelEntityManager;
import org.terasology.entitySystem.entity.internal.BaseEntityRef;

import java.util.Objects;

/**
 * An implementation of EntityRef that deals with entities propagated to a client. These entities may enter and
 * leave relevance over time, and may have a different Entity id each time. NetEntityRef links to them via their
 * network id, and survives them dropping in and out of relevance.
 *
 * @author Immortius
 */
public class NetEntityRef extends BaseEntityRef {

    private int networkId;
    private NetworkSystemImpl networkSystem;

    public NetEntityRef(int networkId, NetworkSystemImpl system, LowLevelEntityManager entityManager) {
        super(entityManager);
        this.networkId = networkId;
        this.networkSystem = system;
    }

    public int getNetworkId() {
        return networkId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof NetEntityRef && getId() == ((NetEntityRef) obj).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(networkId);
    }

    @Override
    public boolean exists() {
        return networkId != 0;
    }

    @Override
    public int getId() {
        return networkSystem.getEntityId(networkId);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        networkId = 0;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
