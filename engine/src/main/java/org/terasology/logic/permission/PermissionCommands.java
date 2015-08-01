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
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.ConsoleCommand;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.console.suggesters.UsernameSuggester;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RegisterSystem
public class PermissionCommands extends BaseComponentSystem {
    @In
    private PermissionManager permissionManager;

    @In
    private EntityManager entityManager;

    @In
    private Console console;

    @In
    private Config config;

    @Command(shortDescription = "Use an one time key to get all* permissions",
            helpText = "The config file contains a one time key which can be used to get all* permissions."
                    + "Please note that the debug permission will only be granted if the debug setting is on.",
            runOnServer = true, requiredPermission = PermissionManager.NO_PERMISSION)
    public String usePermissionKey(@CommandParam("key") String key, @Sender EntityRef client) {
        PermissionConfig permissionConfig = config.getPermission();
        String expectedKey = permissionConfig.getOneTimeAuthorizationKey();

        if (expectedKey != null && !expectedKey.equals("") && key.equals(expectedKey)) {
            permissionConfig.setOneTimeAuthorizationKey("");
            ClientComponent clientComponent = client.getComponent(ClientComponent.class);
            EntityRef clientInfo = clientComponent.clientInfo;
            for (String permission: findAllPermissions()) {
                boolean add = true;
                if (permission.equals(PermissionManager.DEBUG_PERMISSION)) {
                    add = config.getSystem().isDebugEnabled();
                }
                if (add) {
                    permissionManager.addPermission(clientInfo, permission);
                }
            }
            PermissionSetComponent permissionSetComp = clientInfo.getComponent(PermissionSetComponent.class);
            return "Permission key used: You have now the following permissions: " + permissionSetComp.permissions;
        } else {
            return "Key invalid or used";
        }
    }

    private Set<String> findAllPermissions() {
        Set<String> allPermissions = new HashSet<>();
        for (ConsoleCommand command: console.getCommands()) {
            String permission = command.getRequiredPermission();
            if (!permission.equals(PermissionManager.NO_PERMISSION)) {
                allPermissions.add(permission);
            }
        }
        return allPermissions;
    }

    @Command(shortDescription = "Gives specified permission to player",
            helpText = "Gives specified permission to player",
            runOnServer = true, requiredPermission = PermissionManager.USER_MANAGEMENT_PERMISSION)
    public String givePermission(
            @CommandParam(value = "player", suggester = UsernameSuggester.class) String player,
            @CommandParam("permission") String permission,
            @Sender EntityRef requester) {
        boolean permissionGiven = false;

        ClientComponent requesterClientComponent = requester.getComponent(ClientComponent.class);
        EntityRef requesterClientInfo = requesterClientComponent.clientInfo;
        if (!permissionManager.hasPermission(requesterClientInfo, permission)) {
            return String.format("You can't give the permission %s because you don't have it yourself", permission);
        }

        for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
            ClientComponent clientComponent = client.getComponent(ClientComponent.class);
            if (clientHasName(clientComponent, player)) {
                permissionManager.addPermission(clientComponent.clientInfo, permission);
                permissionGiven = true;
            }
        }

        if (permissionGiven) {
            return "Permission " + permission + " added to player " + player;
        } else {
            return "Unable to find player " + player;
        }
    }

    @Command(shortDescription = "Lists all permission the specified player has",
            helpText = "Lists all permission the specified player has",
            runOnServer = true, requiredPermission = PermissionManager.USER_MANAGEMENT_PERMISSION)
    public String listPermissions(@CommandParam(value = "player", suggester = UsernameSuggester.class) String player) {
        for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
            ClientComponent clientComponent = client.getComponent(ClientComponent.class);
            if (clientHasName(clientComponent, player)) {
                EntityRef clientInfo = clientComponent.clientInfo;
                PermissionSetComponent permissionSetComp = clientInfo.getComponent(PermissionSetComponent.class);
                return Objects.toString(permissionSetComp.permissions);
            }
        }
        return "Player not found";
    }

    @Command(shortDescription = "Removes specified permission from player",
            helpText = "Removes specified permission from player",
            runOnServer = true, requiredPermission = PermissionManager.USER_MANAGEMENT_PERMISSION)
    public String removePermission(
            @CommandParam(value = "player", suggester = UsernameSuggester.class) String player,
            @CommandParam("permission") String permission,
            @Sender EntityRef requester) {
        boolean permissionGiven = false;

        ClientComponent requesterClientComponent = requester.getComponent(ClientComponent.class);
        EntityRef requesterClientInfo = requesterClientComponent.clientInfo;
        if (!permissionManager.hasPermission(requesterClientInfo, permission)) {
            return String.format("You can't remove the permission %s because you don't have it yourself", permission);
        }

        for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
            ClientComponent clientComponent = client.getComponent(ClientComponent.class);
            if (clientHasName(clientComponent, player)) {
                permissionManager.removePermission(clientComponent.clientInfo, permission);
                permissionGiven = true;
            }
        }

        if (permissionGiven) {
            return "Permission " + permission + " removed from player " + player;
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
