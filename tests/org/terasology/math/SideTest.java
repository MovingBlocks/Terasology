package org.terasology.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class SideTest {

    @Test
    public void sideInDirection() {
        for (Side side : Side.values()) {
            assertEquals(side, Side.inDirection(side.getVector3i().x, side.getVector3i().y, side.getVector3i().z));
        }
    }
}
