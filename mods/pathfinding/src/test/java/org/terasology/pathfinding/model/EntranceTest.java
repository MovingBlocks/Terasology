package org.terasology.pathfinding.model;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author synopia
 */
public class EntranceTest {
    @Test
    public void testHorizontal() {
        Entrance entrance = new Entrance(null);
        entrance.addToEntrance(1,1);
        Assert.assertTrue(entrance.isPartOfEntrance(1,1));
        Assert.assertTrue(entrance.isPartOfEntrance(2,1));
        Assert.assertFalse(entrance.isPartOfEntrance(3,1));
        entrance.addToEntrance(2,1);
        Assert.assertTrue(entrance.isPartOfEntrance(3,1));
        Assert.assertFalse(entrance.isPartOfEntrance(1,2));
    }

    @Test
    public void testVertical() {
        Entrance entrance = new Entrance(null);
        entrance.addToEntrance(1,1);
        Assert.assertTrue(entrance.isPartOfEntrance(1,1));
        Assert.assertTrue(entrance.isPartOfEntrance(1,2));
        Assert.assertFalse(entrance.isPartOfEntrance(1, 3));
        entrance.addToEntrance(1, 2);
        Assert.assertTrue(entrance.isPartOfEntrance(1, 3));
        Assert.assertFalse(entrance.isPartOfEntrance(2,1));
    }
}
