package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;

/**
 * Main minion component, mostly used for behaviour
 * @author Overdhose>
 */
public final class MinionComponent extends AbstractComponent {

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
