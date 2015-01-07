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

public interface PermissionManager {
    String OPERATOR_PERMISSION = "op";

    /**
     * Adds specified permission to the player (character).
     *
     * @param player     Player (character) to add permission to.
     * @param permission Permission to add.
     */
    void addPermission(EntityRef player, String permission);

    /**
     * Checks if the specified player (character) has said permission.
     * Note: Local player is considered to have all the permissions in all situations.
     *
     * @param player     Player (character) to check.
     * @param permission Permission to check.
     * @return If player (character) has permission.
     */
    boolean hasPermission(EntityRef player, String permission);

    /**
     * Checks if the specified player (character) has permission that is accepted by the specified predicate.
     * Note: Local player is considered to have all the permissions in all situations.
     *
     * @param player              Player (character) to check.
     * @param permissionPredicate Permission predicate to check against.
     * @return If player (character) has permission matching the predicate.
     */
    boolean hasPermission(EntityRef player, Predicate<String> permissionPredicate);

    /**
     * Removes specified permission from the player (character).
     *
     * @param player     Player (character) to remove permission from.
     * @param permission Permission to remove.
     */
    void removePermission(EntityRef player, String permission);
}
