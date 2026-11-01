// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.gestalt.entitysystem.component.Component;

public class NetworkComponent implements Component<NetworkComponent> {
    public ReplicateMode replicateMode = ReplicateMode.RELEVANT;

    // Network identifier for the entity
    @Replicate
    public int networkId;

    @Override
    public void copyFrom(NetworkComponent other) {
        this.networkId = other.networkId;
        this.replicateMode = other.replicateMode;
    }

    public enum ReplicateMode {
        ALWAYS, // Always replicate this entity to all clients
        RELEVANT, // Replicate to client which this entity is relevant to (based on distance)
        OWNER // Always replicate this entity to its owner
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }

    public int getNetworkId() {
        return networkId;
    }
}
