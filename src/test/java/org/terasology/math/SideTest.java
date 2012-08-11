package org.terasology.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
