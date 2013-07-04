package org.terasology.signalling.blockFamily;

import org.terasology.math.Side;
import org.terasology.world.block.family.HorizontalBlockFamilyFactory;
import org.terasology.world.block.family.RegisterBlockFamilyFactory;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterBlockFamilyFactory("leftArchetypeHorizontal")
public class LeftArchetypeHorizontalBlockFamilyFactory extends HorizontalBlockFamilyFactory {
    @Override
    protected Side getArchetypeSide() {
        return Side.LEFT;
    }
}
