// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.permission;

import com.google.common.base.Predicate;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.context.annotation.API;

@API
public interface PermissionManager {
    /**
     * Allows the player to use chat commands.
     */
    String CHAT_PERMISSION = "chat";

    /**
     * Allows the player to use cheats that
     * <ul>
     *     <li>1. have no global impact.</li>
     *     <li>2. don't endanger the stability of the game</li>
     * </ul>
     * The intention is that the permission can be given to all players on a server where players focus on building and
     * not mining.
     */
    String CHEAT_PERMISSION = "cheat";

    /**
     * Used to indicate that something requires no permission.
     */
    String NO_PERMISSION = "";

    /**
     * Allows the player to edit settings of other users.
     *
     * e.g. adding permissions, renaming them etc.
     */
    String USER_MANAGEMENT_PERMISSION = "userManagement";

    /**
     * Allows the player to perform server maintenance tasks like stopping the server.
     */
    String SERVER_MANAGEMENT_PERMISSION = "serverManagement";

    /**
     * Allows the player to use debug commands which are not intended to be used on a real server.
     */
    String DEBUG_PERMISSION = "debug";

    /**
     * Adds specified permission to the player (client info entity).
     *
     * @param player     Player (client info entity) to add permission to.
     * @param permission Permission to add.
     */
    void addPermission(EntityRef player, String permission);

    /**
     * Checks if the specified player (client info entity) has said permission.
     * Note: Local player is considered to have all the permissions in all situations.
     *
     * @param player     Player (client info entity) to check.
     * @param permission Permission to check.
     * @return If player (client info entity) has permission.
     */
    boolean hasPermission(EntityRef player, String permission);

    /**
     * Checks if the specified player (client info entity) has permission that is accepted by the specified predicate.
     * Note: Local player is considered to have all the permissions in all situations.
     *
     * @param player              Player (client info entity) to check.
     * @param permissionPredicate Permission predicate to check against.
     * @return If player (client info entity) has permission matching the predicate.
     */
    boolean hasPermission(EntityRef player, Predicate<String> permissionPredicate);

    /**
     * Removes specified permission from the player (client info entity).
     *
     * @param player     Player (client info entity) to remove permission from.
     * @param permission Permission to remove.
     */
    void removePermission(EntityRef player, String permission);
}
