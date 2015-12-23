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
package org.terasology.logic.console.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.GameEngine;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.console.suggesters.UsernameSuggester;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.PlayerUtil;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.ClientInfoComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.StorageManager;
import org.terasology.registry.In;
import org.terasology.world.chunks.ChunkProvider;

/**
 * Commands to administer a remote server
 *
 */
@RegisterSystem
public class ServerCommands extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(ServerCommands.class);

    @In
    private EntityManager entityManager;

    @In
    private StorageManager storageManager;

    @In
    private ChunkProvider chunkProvider;

    @In
    private NetworkSystem networkSystem;

    @In
    private Config config;

    @In
    private GameEngine gameEngine;

    @Command(shortDescription = "Shutdown the server", runOnServer = true,
            requiredPermission = PermissionManager.SERVER_MANAGEMENT_PERMISSION)
    public String shutdownServer(@Sender EntityRef sender) {

        // TODO: verify permissions of sender

        EntityRef clientInfo = sender.getComponent(ClientComponent.class).clientInfo;
        DisplayNameComponent name = clientInfo.getComponent(DisplayNameComponent.class);

        logger.info("Shutdown triggered by {}", name.name);

        gameEngine.shutdown();

        return "Server shutdown triggered";
    }

    @Command(shortDescription = "Kick user by name", runOnServer = true,
            requiredPermission = PermissionManager.USER_MANAGEMENT_PERMISSION)
    public String kickUser(@CommandParam("username") String username) {

        for (EntityRef clientEntity : entityManager.getEntitiesWith(ClientComponent.class)) {
            EntityRef clientInfo = clientEntity.getComponent(ClientComponent.class).clientInfo;

            DisplayNameComponent name = clientInfo.getComponent(DisplayNameComponent.class);
            if (username.equals(name.name)) {

                return kick(clientEntity);
            }
        }

        throw new IllegalArgumentException("No such user '" + username + "'");
    }

    @Command(shortDescription = "Rename a user", runOnServer = true,
            requiredPermission = PermissionManager.USER_MANAGEMENT_PERMISSION)
    public String renameUser(
            @CommandParam(value = "userName", suggester = UsernameSuggester.class) String userName,
            @CommandParam(value = "newUserName") String newUserName) {
        Iterable<EntityRef> clientInfoEntities = entityManager.getEntitiesWith(ClientInfoComponent.class);
        for (EntityRef clientInfo : clientInfoEntities) {
            DisplayNameComponent nameComp = clientInfo.getComponent(DisplayNameComponent.class);
            if (newUserName.equals(nameComp.name)) {
                throw new IllegalArgumentException("New user name is already in use");
            }
        }


        for (EntityRef clientInfo : clientInfoEntities) {
            DisplayNameComponent nameComp = clientInfo.getComponent(DisplayNameComponent.class);
            if (userName.equals(nameComp.name)) {
                nameComp.name = newUserName;
                clientInfo.saveComponent(nameComp);
                return "User " + userName + " has been renamed to " + newUserName;
            }
        }

        throw new IllegalArgumentException("No such user '" + userName + "'");
    }

    @Command(shortDescription = "Kick user by ID", runOnServer = true,
            requiredPermission = PermissionManager.USER_MANAGEMENT_PERMISSION)
    public String kickUserByID(@CommandParam("userId") int userId) {

        // TODO: verify permissions of sender

        for (EntityRef clientEntity : entityManager.getEntitiesWith(ClientComponent.class)) {
            EntityRef clientInfo = clientEntity.getComponent(ClientComponent.class).clientInfo;
            NetworkComponent nc = clientInfo.getComponent(NetworkComponent.class);

            if (userId == nc.getNetworkId()) {
                return kick(clientEntity);
            }
        }

        throw new IllegalArgumentException("No such user with ID " + userId);
    }

    @Command(shortDescription = "List users",
            requiredPermission = PermissionManager.USER_MANAGEMENT_PERMISSION)
    public String listUsers() {

        StringBuilder stringBuilder = new StringBuilder();

        for (EntityRef clientInfo : entityManager.getEntitiesWith(ClientInfoComponent.class)) {

            DisplayNameComponent dnc = clientInfo.getComponent(DisplayNameComponent.class);
            NetworkComponent nc = clientInfo.getComponent(NetworkComponent.class);

            String playerText = PlayerUtil.getColoredPlayerName(clientInfo);

            String line = String.format("%s - %s (%d)", playerText, dnc.description, nc.getNetworkId());

            stringBuilder.append(line);
            stringBuilder.append(Console.NEW_LINE);
        }

        return stringBuilder.toString();
    }

    private String kick(EntityRef clientEntity) {
        Client client = networkSystem.getOwner(clientEntity);

        if (!client.isLocal()) {
            EntityRef clientInfo = clientEntity.getComponent(ClientComponent.class).clientInfo;
            DisplayNameComponent name = clientInfo.getComponent(DisplayNameComponent.class);

            logger.info("Kicking user {}", name.name);

            networkSystem.forceDisconnect(client);
            return "User kick triggered for '" + name.name + "'";
        }

        return "Request declined";
    }

    @Command(shortDescription = "Triggers the creation of a save game", runOnServer = true,
            requiredPermission = PermissionManager.SERVER_MANAGEMENT_PERMISSION)
    public void save() {
        storageManager.requestSaving();
    }

    @Command(shortDescription = "Invalidates the specified chunk and recreates it (requires storage manager disabled)", runOnServer = true)
    public String reloadChunk(@CommandParam("x") int x, @CommandParam("y") int y, @CommandParam("z") int z) {
        Vector3i pos = new Vector3i(x, y, z);
        if (config.getSystem().isWriteSaveGamesEnabled()) {
            return "Writing save games is enabled! Invalidating chunk has no effect";
        }
        boolean success = chunkProvider.reloadChunk(pos);
        return success
                ? "Cleared chunk " + pos + " from cache and triggered reload"
                : "Chunk " + pos + " did not exist in the cache";
    }

    @Command(shortDescription = "Deletes the current world and generated new chunks", runOnServer = true)
    public void purgeWorld() {
        chunkProvider.purgeWorld();
    }
}
