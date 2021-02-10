/*
 * Copyright 2014 MovingBlocks
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
import org.terasology.entitySystem.entity.internal.DefaultRefStrategy;
import org.terasology.network.NetworkComponent;

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
