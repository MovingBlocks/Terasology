package org.terasology.mods.miniions.components;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 9/05/12
 * Time: 17:50
 * Similar to toolbar, represents 9 slots that hold the active miniions instead of items
 */
public class MinionBarComponent implements Component {

    public List<EntityRef> minionSlots = Lists.newArrayList();

    public MinionBarComponent() {
    }

    public MinionBarComponent(int numSlots) {
        for (int i = 0; i < numSlots; ++i) {
            minionSlots.add(EntityRef.NULL);
        }
    }
}
