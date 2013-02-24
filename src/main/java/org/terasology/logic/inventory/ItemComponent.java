/*
 * Copyright 2013 Moving Blocks
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

import java.util.Map;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;

import com.google.common.collect.Maps;
import org.terasology.network.Replicate;
import org.terasology.network.ReplicateType;

/**
 * Item data is stored using this component
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class ItemComponent implements Component {
    /**
     * The display name of this item
     */
    @Replicate(value = ReplicateType.SERVER_TO_CLIENT, initialOnly = true)
    public String name = "";

    /**
     * Should this item be rendered? Some items have an inventory icon but no "held" representation
     */
    @Replicate(value = ReplicateType.SERVER_TO_CLIENT, initialOnly = true)
    public boolean renderWithIcon = false;

    /**
     * Name of the icon this item should be rendered with
     */
    @Replicate(value = ReplicateType.SERVER_TO_CLIENT, initialOnly = true)
    public String icon = "";

    /**
     * If this item is stackable, it should have a unique ID (so alike stacks can be merged)
     */
    @Replicate(value = ReplicateType.SERVER_TO_CLIENT, initialOnly = true)
    public String stackId = "";

    /**
     * How many of said item are there in this stack
     */
    @Replicate(ReplicateType.SERVER_TO_CLIENT)
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
    @Replicate(ReplicateType.SERVER_TO_OWNER)
    public UsageType usage = UsageType.NONE;

    /**
     * Does this item drop in quantity on usage (stacks of things would, tools would not)
     */
    public boolean consumedOnUse = false;

    /**
     * Setting for how much damage would be inflicted on attack (for instance to damage a block)
     */
    public int baseDamage = 1;

    // TODO: Should use block categories, rather than specific block names (or support both)
    /**
     * Map for what this item would have a bonus against (shovels dig faster than hammers)
     */
    private Map<String, Integer> perBlockDamageBonus = Maps.newHashMap();

    public Map<String, Integer> getPerBlockDamageBonus() {

        return perBlockDamageBonus;
    }

}
