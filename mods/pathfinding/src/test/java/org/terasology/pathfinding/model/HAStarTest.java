package org.terasology.pathfinding.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Vector3i;

import java.util.*;

/**
 * @author synopia
 */
public class HAStarTest {
    private WalkableBlock start;
    private WalkableBlock end;

    @Test
    public void stairsClosed() {
        executeExample(new String[]{
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXX   XXX",
                "XXX   XXX|   XXX   |         |XXX   XXX",
                "XXX   XXX|         |   XXX   |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
                "?        |         |         |         ",
                "         |         |         |         ",
                "         |         |         |         ",
                "         |         |         |         ",
                "         |         |         |         ",
                "         |         |         |         ",
                "         |         |         |         ",
                "         |         |         |        !",
        });
    }
    @Test
    public void stairs() {
        executeExample(new String[]{
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXX   XXX",
                "XXX   XXX|   XXX   |         |XXX   XXX",
                "XXX   XXX|         |   XXX   |XXX   XXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
                "?1       |         |         |         ",
                "  2      |         |         |         ",
                "   3     |         |         |         ",
                "    4    |         |         |         ",
                "         |     5   |         |         ",
                "         |         |     6   |      7  ",
                "         |         |         |       8 ",
                "         |         |         |        !",
        });
    }
    @Test
    public void stairs2() {
        executeExample(new String[]{
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXX XXXXX|         |         |XXXXX XXX",
                "XXX XXXXX|   X     |         |XXX XXXXX|     X   |         |XXXXX XXX",
                "XXX XXXXX|         |   X     |XXX XXXXX|         |     X   |XXXXX XXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
                "?        |         |         |         |         |         |         ",
                " 1       |         |         |         |         |         |         ",
                "  2      |         |         |         |         |         |         ",
                "   3     |         |         |     7   |         |         |         ",
                "         |   4     |         |    6    |     8   |         |         ",
                "         |         |   5     |         |         |     9   |      0  ",
                "         |         |         |         |         |         |       a ",
                "         |         |         |         |         |         |        !",
        });
    }
    @Test
    public void stairsClosed2() {
        executeExample(new String[]{
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXX XXXXX|         |         |XXXXX XXX",
                "XXX XXXXX|   X     |         |XXX XXXXX|     X   |         |XXXXX XXX",
                "XXX XXXXX|         |   X     |XXXXXXXXX|         |     X   |XXXXX XXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
                "?        |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |         ",
                "         |         |         |         |         |         |        !",
        });
    }
    @Test
    public void simple() {
        executeExample( new String[]{
                "XXX",
                "XXX",
                "XXX",
        }, new String[]{
                "?  ",
                " 1 ",
                "  !"
        });
        executeExample( new String[]{
                "XXXXXXXXXXXXXXXX",
                "             XXX",
                "XXXXXXXXXXXXXXXX",
                "XXX             ",
                "XXXXXXXXXXXXXXXX",
                "             XXX",
        }, new String[]{
                "?1234567890ab   ",
                "             c  ",
                "   mlkjihgfed  ",
                "  n             ",
                "   opqrstuvwxyz ",
                "               !",
        });

    }

    private void executeExample( String[] ground, String[] pathData ) {
        final TestHelper helper = new TestHelper();
        helper.setGround(ground);
        helper.map.update();
        final Map<Integer, Vector3i> expected = new HashMap<Integer, Vector3i>();
        helper.parse(new TestHelper.Runner() {
            @Override
            public char run(int x, int y, int z, char value) {

                switch (value) {
                    case '?':
                        start = helper.map.getBlock(x, y, z);
                        break;
                    case '!':
                        end = helper.map.getBlock(x, y, z);
                        break;
                    default:
                        if( value=='0') {
                            expected.put(10, helper.map.getBlock(x, y, z).getBlockPosition());
                        } else  if( value>'0' && value<='9') {
                            expected.put(value-'0', helper.map.getBlock(x, y, z).getBlockPosition());
                        } else if( value>='a' && value<='z') {
                            expected.put(value-'a'+11, helper.map.getBlock(x, y, z).getBlockPosition());
                        } else if( value>='A' && value<='Z') {
                            expected.put(value-'A'+11+27, helper.map.getBlock(x, y, z).getBlockPosition());
                        }
                        break;
                }
                return 0;
            }
        }, pathData);
        expected.put(0, start.getBlockPosition());
//        expected.put(expected.size(), end.getBlockPosition());

        HAStar haStar = new HAStar();

        haStar.run(end, start);
        List<WalkableBlock> path = haStar.getPath();
        int pos = 0;
        Assert.assertEquals(expected.size(), path.size());
        for (WalkableBlock block : path) {
            Vector3i p = expected.get(pos);
            Assert.assertEquals(p, block.getBlockPosition());
            pos++;
        }
    }

}
