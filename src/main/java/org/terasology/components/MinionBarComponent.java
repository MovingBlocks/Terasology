package org.terasology.components;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.EntityRef;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 9/05/12
 * Time: 17:50
 * Similar to toolbar, represents 9 slots that hold the active miniions instead of items
 *
 */
public class MinionBarComponent extends AbstractComponent {

    public List<EntityRef> MinionSlots = Lists.newArrayList();

    public MinionBarComponent(){}

    public MinionBarComponent(int numSlots) {
        for (int i = 0; i < numSlots; ++i) {
            MinionSlots.add(EntityRef.NULL);
        }
    }
}
