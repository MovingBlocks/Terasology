// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.metadata;

public enum NetworkEventType {

    /**
     * This event is not a network event
     */
    NONE,

    /**
     * This event is propagated from client to server
     */
    SERVER,

    /**
     * This event is propagated from the server to the client that owns the target entity
     */
    OWNER,

    /**
     * This event is propagated from the server to all clients.
     */
    BROADCAST
}
