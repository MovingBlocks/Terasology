package org.terasology.components;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;

import java.util.Map;

/**
 * Item data is stored using this component
 * @author Immortius <immortius@gmail.com>
 */
public final class ItemComponent implements Component {
    /** The display name of this item */
    public String name = "";

    /** Should this item be rendered? Some items have an inventory icon but no "held" representation */
    public boolean renderWithIcon = false;

    /** Name of the icon this item should be rendered with */
    public String icon = "";

    /** If this item is stackable, it should have a unique ID (so alike stacks can be merged) */
    public String stackId = "";

    /** How many of said item are there in this stack */
    public byte stackCount = 1;

    /** The entity that contains this one (if appropriate, defaults to this item not being contained by anything) */
    public EntityRef container = EntityRef.NULL;

    /** Enum to hold item usage possibilities */
    public enum UsageType {
        NONE,
        ON_USER,
        ON_BLOCK,
        ON_ENTITY,
        IN_DIRECTION
    }

    /** Usage setting for this item */
    public UsageType usage = UsageType.NONE;

    /** Does this item drop in quantity on usage (stacks of things would, tools would not) */
    public boolean consumedOnUse = false;

    /** Setting for how much damage would be inflicted on attack (for instance to damage a block) */
    public int baseDamage = 1;

    // TODO: Should use block categories, rather than specific block names (or support both)
    /** Map for what this item would have a bonus against (shovels dig faster than hammers) */
    private Map<String, Integer> perBlockDamageBonus = Maps.newHashMap();

    public Map<String, Integer> getPerBlockDamageBonus() {

        return perBlockDamageBonus;
    }

}
