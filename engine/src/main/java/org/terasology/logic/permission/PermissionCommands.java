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
package org.terasology.logic.permission;

import org.terasology.config.Config;
import org.terasology.config.PermissionConfig;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.console.commands.referenced.CommandDefinition;
import org.terasology.logic.console.commands.referenced.CommandParameter;
import org.terasology.network.ClientComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;

@RegisterSystem
public class PermissionCommands extends BaseComponentSystem {
    @In
    private PermissionManager permissionManager;
    @In
    private EntityManager entityManager;

    @CommandDefinition(shortDescription = "Use an one time key to get op permission",
            helpText = "The config file contains a one time key which can be used to get op permission",
            runOnServer = true, requiredPermission = "")
    public String usePermissionKey(@CommandParameter("key") String key, EntityRef client) {
        PermissionConfig permissionConfig = CoreRegistry.get(Config.class).getPermission();
        String expectedKey = permissionConfig.getOneTimeAuthorizationKey();

        if (expectedKey != null && !expectedKey.equals("") && key.equals(expectedKey)) {
            permissionConfig.setOneTimeAuthorizationKey("");
            ClientComponent clientComponent = client.getComponent(ClientComponent.class);
            permissionManager.addPermission(clientComponent.character, PermissionManager.OPERATOR_PERMISSION);
            return "Permission key used: You have now \"op\" rights";
        } else {
            return "Key invalid or used";
        }
    }

    @CommandDefinition(shortDescription = "Gives specified permission to player",
            helpText = "Gives specified permission to player",
            runOnServer = true)
    public String givePermission(
            @CommandParameter("player") String player,
            @CommandParameter("permission") String permission) {
        boolean permissionGiven = false;

        for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
            ClientComponent clientComponent = client.getComponent(ClientComponent.class);
            if (clientHasName(clientComponent, player)) {
                permissionManager.addPermission(clientComponent.character, permission);
                permissionGiven = true;
            }
        }

        if (permissionGiven) {
            return "Permission " + permission + " added to player " + player;
        } else {
            return "Unable to find player " + player;
        }
    }

    @CommandDefinition(shortDescription = "Removes specified permission from player",
            helpText = "Removes specified permission from player",
            runOnServer = true)
    public String removePermission(
            @CommandParameter("player") String player,
            @CommandParameter("permission") String permission) {
        boolean permissionGiven = false;

        for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
            ClientComponent clientComponent = client.getComponent(ClientComponent.class);
            if (clientHasName(clientComponent, player)) {
                permissionManager.removePermission(clientComponent.character, permission);
                permissionGiven = true;
            }
        }

        if (permissionGiven) {
            return "Permission " + permission + " removed to player " + player;
        } else {
            return "Unable to find player " + player;
        }
    }

    private boolean clientHasName(ClientComponent client, String playerName) {
        EntityRef clientInfo = client.clientInfo;
        if (clientInfo != null) {
            String name = clientInfo.getComponent(DisplayNameComponent.class).name;
            return playerName.equals(name);
        }
        return false;
    }
}
