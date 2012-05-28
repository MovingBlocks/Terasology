package org.terasology.mods.miniions.components;

import org.terasology.entitySystem.Component;
import org.terasology.mods.miniions.minionenum.MinionBehaviour;

/**
 * Allows an entity to store items
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class MinionComponent implements Component {

    public String icon = "";
    public MinionBehaviour minionBehaviour = MinionBehaviour.Stay;

    public MinionComponent() {
    }

}
