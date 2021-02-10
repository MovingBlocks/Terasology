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

import com.google.common.base.Predicate;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.registry.Share;

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
