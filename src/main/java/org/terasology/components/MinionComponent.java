package org.terasology.components;

import org.terasology.entitySystem.Component;

/**
 * Allows an entity to store items
 * @author Immortius <immortius@gmail.com>
 */
public final class MinionComponent implements Component {

    public String icon = "";
    public MinionBehaviour minionBehaviour = MinionBehaviour.Follow;

    public enum MinionBehaviour{
        Follow,
        Move,
        Gather,
        Inventory,
        Disappear
    }

    public MinionComponent() {}

}
