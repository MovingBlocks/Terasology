package org.terasology.components;

import org.terasology.entitySystem.Component;

/**
 * Allows an entity to store items
 * @author Immortius <immortius@gmail.com>
 */
public final class MinionComponent implements Component {

    public String icon = "";
    public MinionBehaviour minionBehaviour = MinionBehaviour.Stay;

    public enum MinionBehaviour{
        Stay,
        Follow,
        Move,
        Gather,
        Patrol,
        Test,
        Inventory,
        Disappear,
        Clear
    }

    public MinionComponent() {}

}
