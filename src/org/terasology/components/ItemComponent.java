package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class ItemComponent extends AbstractComponent {
    public String name = "";
    public String icon = "";
    public String stackId = "";
    public int stackCount = 1;

    public EntityRef container;
    public UsageType usage = UsageType.None;
    public boolean consumedOnUse = false;

    public enum UsageType {
        None,
        OnUser,
        OnBlock,
        OnEntity,
        InDirection
    }

}
