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

package org.terasology.logic.inventory;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;
import org.terasology.rendering.assets.texture.TextureRegionAsset;

/**
 * Item data is stored using this component
 *
 */
public final class ItemComponent implements Component {
    /**
     * Name of the icon this item should be rendered with
     */
    @Replicate(value = FieldReplicateType.SERVER_TO_CLIENT, initialOnly = true)
    public TextureRegionAsset<?> icon;

    //TODO: move this to a separate component alongside the inventory system
    /**
     * If this item is stackable, it should have a unique ID (so alike stacks can be merged)
     */
    @Replicate(value = FieldReplicateType.SERVER_TO_CLIENT, initialOnly = true)
    public String stackId = "";

    //TODO: move this to a separate component alongside the inventory system
    @Replicate(value = FieldReplicateType.SERVER_TO_CLIENT, initialOnly = true)
    public byte maxStackSize = 99;

    //TODO: move this to a separate component alongside the inventory system
    /**
     * How many of said item are there in this stack
     */
    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public byte stackCount = 1;

    /**
     * Enum to hold item usage possibilities
     */
    public enum UsageType {
        NONE,
        ON_USER,
        ON_BLOCK,
        ON_ENTITY,
        IN_DIRECTION
    }

    /**
     * Usage setting for this item
     */
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public UsageType usage = UsageType.NONE;

    //TODO: move this to a separate component
    /**
     * Does this item drop in quantity on usage (stacks of things would, tools would not)
     */
    public boolean consumedOnUse;

    //TODO: move this to a separate component alongside the health system
    /**
     * Setting for how much damage would be inflicted on attack (for instance to damage a block)
     */
    public int baseDamage = 1;

    //TODO: move this to a separate component alongside the health system
    public Prefab damageType;

    public Prefab pickupPrefab;

    public int cooldownTime = 200;
}
