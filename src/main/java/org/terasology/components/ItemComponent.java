package org.terasology.components;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.EntityRef;

import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class ItemComponent extends AbstractComponent {
    public String name = "";
    public boolean renderWithIcon = false;
    public String icon = "";
    public String stackId = "";

    public byte stackCount = 1;
    public EntityRef container = EntityRef.NULL;

    // Usage settings
    public enum UsageType {
        None,
        OnUser,
        OnBlock,
        OnEntity,
        InDirection
    }

    public UsageType usage = UsageType.None;
    public boolean consumedOnUse = false;


    // Attack related settings
    public int baseDamage = 1;
    // TODO: Should use block categories, rather than specific block names (or support both)
    private Map<String, Integer> perBlockDamageBonus = Maps.newHashMap();

    public Map<String, Integer> getPerBlockDamageBonus() {
        return perBlockDamageBonus;
    }

}
