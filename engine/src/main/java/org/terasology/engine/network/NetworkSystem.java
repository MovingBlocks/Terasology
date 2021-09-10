// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.metadata.EventLibrary;
import org.terasology.engine.network.exceptions.HostingFailedException;
import org.terasology.nui.Color;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.block.internal.BlockRegistrationListener;
import org.terasology.engine.world.chunks.remoteChunkProvider.RemoteChunkProvider;

/**
 * Interface for the network system
 *
 */
// TODO: Refactor the core gameplay components like the list of players into a separate system.
public interface NetworkSystem extends BlockRegistrationListener {

    void host(int port, boolean dedicatedServer) throws HostingFailedException;

    JoinStatus join(String address, int port) throws InterruptedException;

    void shutdown();

    Client joinLocal(String preferredName, Color color);

    void update();

    NetworkMode getMode();

    Server getServer();

    Iterable<Client> getPlayers();

    Client getOwner(EntityRef entity);

    EntityRef getOwnerEntity(EntityRef entity);

    void setRemoteWorldProvider(RemoteChunkProvider remoteWorldProvider);

    void connectToEntitySystem(EngineEntityManager entityManager, EventLibrary library, BlockEntityRegistry blockEntityRegistry);

    int getIncomingMessagesDelta();

    int getIncomingBytesDelta();

    int getOutgoingMessagesDelta();

    int getOutgoingBytesDelta();

    void forceDisconnect(Client client);

    void setContext(Context context);
}
