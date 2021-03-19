// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.inventory;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;

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
