package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;

import java.util.List;

/**
 * Allows an entity to store items
 * @author Immortius <immortius@gmail.com>
 */
public final class MinionComponent extends AbstractComponent {

    public String icon = "";
    public MinionBehaviour minionBehaviour = MinionBehaviour.Follow;

    public enum MinionBehaviour{
        Move,
        Follow,
        Gather,
        Disappear
    }

    public MinionComponent() {}

}
