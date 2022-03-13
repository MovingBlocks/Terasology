// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.permission;

import com.google.common.base.Predicate;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;

import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share(PermissionManager.class)
public class PermissionSystem extends BaseComponentSystem implements PermissionManager {
    @In
    private LocalPlayer localPlayer;

    @Override
    public void addPermission(EntityRef player, String permission) {
        PermissionSetComponent permissionSet = player.getComponent(PermissionSetComponent.class);
        if (permissionSet != null) {
            permissionSet.permissions.add(permission);
            player.saveComponent(permissionSet);
        }
    }

    @Override
    public boolean hasPermission(EntityRef player, String permission) {
        // Local player has all permissions in every situation
        if (isLocal(player)) {
            return true;
        }

        PermissionSetComponent permissionSet = player.getComponent(PermissionSetComponent.class);
        return permissionSet != null && permissionSet.permissions.contains(permission);
    }

    @Override
    public boolean hasPermission(EntityRef player, Predicate<String> permissionPredicate) {
        // Local player has all permission in every situation
        if (isLocal(player)) {
            return true;
        }

        PermissionSetComponent permissionSet = player.getComponent(PermissionSetComponent.class);
        return permissionSet != null && predicateMatches(permissionPredicate, permissionSet.permissions);
    }

    private boolean predicateMatches(Predicate<String> permissionPredicate, Set<String> permissions) {
        for (String permission : permissions) {
            if (permissionPredicate.apply(permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removePermission(EntityRef player, String permission) {
        PermissionSetComponent permissionSet = player.getComponent(PermissionSetComponent.class);
        if (permissionSet != null) {
            permissionSet.permissions.remove(permission);
            player.saveComponent(permissionSet);
        }
    }

    /**
     *
     * @param player client info entity of the player
     *
     * @return true if it is the local player
     */
    private boolean isLocal(EntityRef player) {
        return localPlayer != null && localPlayer.getClientInfoEntity().equals(player);
    }
}
