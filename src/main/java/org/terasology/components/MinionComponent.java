package org.terasology.components;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.model.inventory.Icon;

import java.util.List;

/**
 * Allows an entity to store items
 * @author Immortius <immortius@gmail.com>
 */
public final class MinionComponent extends AbstractComponent {

    public List<EntityRef> MinionSlots = Lists.newArrayList();
    public String icon = "";
    public MinionBehaviour minionBehaviour = MinionBehaviour.Follow;

    public enum MinionBehaviour{
        Move,
        Follow,
        Gather,
        Disappear
    }

    public MinionComponent() {}

    public MinionComponent(int numSlots) {
        for (int i = 0; i < numSlots; ++i) {
            MinionSlots.add(EntityRef.NULL);
        }
    }
}
