package org.terasology.pathfinding.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author synopia
 */
public class RegionFinderTest {
    @Test
    public void testFindRegion() {
        assertRegions(new String[]{"X"}, new String[]{"0"});
        assertRegions(new String[]{"XX", "XX"}, new String[]{"00", "00"});
        assertRegions(new String[]{
                "XXX",
                "XX ",
                "XXX"
        }, new String[]{
                "000",
                "00 ",
                "000"
        });
        assertRegions(new String[]{
                "XXX",
                " XX",
                "XXX"
        }, new String[]{
                "000",
                " 00",
                "000"
        });
        assertRegions(new String[]{
                "XXX",
                "X X",
                "XXX"
        }, new String[]{
                "000",
                "1 2",
                "333"
        }, new int[][]{ {1,2}, {0, 3}, {0, 3}, {1, 2} });
        assertRegions(new String[]{
                "X X X",
                "XXXXX",
                "X X X"
        }, new String[]{
                "0 1 2",
                "33333",
                "4 5 6"
        }, new int[][]{ {3}, {3}, {3}, {0,1,2,4,5,6}, {3}, {3}, {3}});
        assertRegions(new String[]{
                "XXXXX",
                "XXXXX",
                "XXXXX"
        }, new String[]{
                "00000",
                "00000",
                "00000"
        });
    }

    @Test
    public void bigTest() {
        assertRegions(new String[]{
                "XXXXXXXXX|         |         ",
                "XXXXXXXXX|         |         ",
                "XXXXXXXXX|         |         ",
                "XXXXXXXXX|   XXX   |   XXX   ",
                "XXXXXXXXX|   XXX   |   XXX   ",
                "XXXXXXXXX|   XXX   |   XXX   ",
                "XXXXXXXXX|         |         ",
                "XXXXXXXXX|         |         ",
                "XXXXXXXXX|         |         ",
        }, new String[]{
                "000000000|         |         ",
                "000000000|         |         ",
                "000000000|         |         ",
                "111   333|         |   222   ",
                "111   333|         |   222   ",
                "111   333|         |   222   ",
                "444444444|         |         ",
                "444444444|         |         ",
                "444444444|         |         ",
        });
        assertRegions(new String[]{
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
                "000000000|         |         |111111111",
                "000000000|         |         |111111111",
                "000000000|         |         |111111111",
                "000000000|         |         |111111111",
                "000000000|         |         |111111111",
                "000000000|         |         |111111111",
                "000000000|         |         |111111111",
                "000000000|         |         |111111111",
                "000000000|         |         |111111111",
        });
        assertRegions(new String[]{
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXX   XXX",
                "XXX   XXX|   XXX   |         |XXX   XXX",
                "XXX   XXX|         |   XXX   |XXX   XXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
                "000000000|         |         |111111111",
                "000000000|         |         |111111111",
                "000000000|         |         |111111111",
                "000000000|         |         |222   333",
                "444   666|   555   |         |222   333",
                "444   666|         |   777   |777   777",
                "888888888|         |         |777777777",
                "888888888|         |         |777777777",
                "888888888|         |         |777777777",
        });
    }

    private void assertRegions(String[] data, String[] regions) {
        assertRegions(data, regions, null);
    }
    private void assertRegions(String[] data, String[] regions, int[][] connections) {
        final TestHelper helper = new TestHelper();
        helper.setGround(
                data
        );
        new WalkableBlockFinder(helper.world).findWalkableBlocks(helper.map);
        final FloorFinder finder = new FloorFinder(helper.world);
        finder.findRegions(helper.map);
        String[] actual = helper.evaluate(new TestHelper.Runner() {
            @Override
            public char run(int x, int y, int z, char value) {
                WalkableBlock block = helper.map.getBlock(x, y, z);
                if (block != null) {
                    Region region = finder.region(block);
                    return (char) ('0' + region.id);
                }
                return ' ';
            }
        });
        Assert.assertArrayEquals(regions, actual);
        if( connections!=null ) {
            for (Region region : finder.regions()) {
                Set<Integer> all = new HashSet<Integer>();
                for (Region neighbor : region.getNeighborRegions()) {
                    all.add(neighbor.id);
                }
                for (int id : connections[region.id]) {
                    Assert.assertTrue("region "+id+" not found in neighbors of region "+region.id, all.remove(id));
                }
                Assert.assertEquals(0, all.size());
            }
        }
    }
}
