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

package org.terasology.network;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.network.exceptions.HostingFailedException;
import org.terasology.rendering.nui.Color;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.internal.BlockRegistrationListener;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;

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
}
