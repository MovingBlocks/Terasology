package org.terasology.pathfinding.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author synopia
 */
public class ContourFinderTest {
    @Test
    public void testStairs(){
        assertContour(new String[]{
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXX XXXXX|         |         |XXXXX XXX",
                "XXXXXXXXX|         |         |XXX XXXXX|         |         |XXXXX XXX",
                "XXX XXXXX|         |         |XXX XXXXX|         |         |XXXXX XXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
                "CIIIIIIIC|         |         |CIIIIIIIC|         |         |CIIIIIIIC",
                "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
                "IIIIIIIII|         |         |IICICIIII|         |         |IIIICICII",
                "IIIIIIIII|         |         |III IIIII|         |         |IIIII III",
                "IICICIIII|         |         |III IIIII|         |         |IIIII III",
                "III IIIII|         |         |III IIIII|         |         |IIIII III",
                "IICICIIII|         |         |IICICIIII|         |         |IIIICICII",
                "CIIIIIIIC|         |         |CIIIIIIIC|         |         |CIIIIIIIC",
        });
        assertContour(new String[]{
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXX XXXXX|         |         |XXXXX XXX",
                "XXX XXXXX|   X     |         |XXX XXXXX|     X   |         |XXXXX XXX",
                "XXX XXXXX|         |   X     |XXX XXXXX|         |     X   |XXXXX XXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
                "XXXXXXXXX|         |         |XXXXXXXXX|         |         |XXXXXXXXX",
        }, new String[]{
                "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
                "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
                "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
                "IIIIIIIII|         |         |III IIIII|         |         |IIIII III",
                "III IIIII|   C     |         |III I III|     C   |         |IIIII III",
                "III IIIII|         |   C     |III I III|         |     C   |IIIII III",
                "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
                "IIIIIIIII|         |         |IIIIIIIII|         |         |IIIIIIIII",
        });

    }
    @Test
    public void testSingleLine(){
        assertContour(new String[]{
                "XX",
        }, new String[]{
                "CC",
        });
        assertContour(new String[]{
                "XXXXXXX",
        }, new String[]{
                "CIIIIIC",
        });
        assertContour(new String[]{
                "         ",
                " XXXXXXX ",
                "         ",
        }, new String[]{
                "         ",
                " CIIIIIC ",
                "         ",
        });
        assertContour(new String[]{
                "         ",
                " XXXXXXX ",
                "    X    ",
                "    X    ",
                "    X    ",
        }, new String[]{
                "         ",
                " CIIIIIC ",
                "    I    ",
                "    I    ",
                "    C    ",
        });
    }

    @Test
    public void testDoubleLine(){
        assertContour(new String[]{
                "XXXXXXX",
                "XXXXXXX",
        }, new String[]{
                "CIIIIIC",
                "CIIIIIC",
        });
        assertContour(new String[]{
                "XX",
                "XX",
                "XX",
                "XX",
        }, new String[]{
                "CC",
                "II",
                "II",
                "CC",
        });
        assertContour(new String[]{
                "         ",
                " XXXXXXX ",
                " XXXXXXX ",
                "         ",
        }, new String[]{
                "         ",
                " CIIIIIC ",
                " CIIIIIC ",
                "         ",
        });
        assertContour(new String[]{
                "          ",
                " XXXXXXXX ",
                " XXXXXXXX ",
                "    XX    ",
                "    XX    ",
                "    XX    ",
        }, new String[]{
                "          ",
                " CIIIIIIC ",
                " CIIIIIIC ",
                "    II    ",
                "    II    ",
                "    CC    ",
        });
    }
    @Test
    public void testDoubleLineInverted(){
        assertContour(new String[]{
                "XXXXXXXXX",
                "XXXXXXXXX",
                "XX     XX",
                "XX     XX",
                "XXXXXXXXX",
                "XXXXXXXXX",
        }, new String[]{
                "IIIIIIIII",
                "IIIIIIIII",
                "II     II",
                "II     II",
                "IIIIIIIII",
                "IIIIIIIII",
        });
        assertContour(new String[]{
                "XX",
                "XX",
                "XX",
                "XX",
        }, new String[]{
                "CC",
                "II",
                "II",
                "CC",
        });
        assertContour(new String[]{
                "         ",
                " XXXXXXX ",
                " XXXXXXX ",
                "         ",
        }, new String[]{
                "         ",
                " CIIIIIC ",
                " CIIIIIC ",
                "         ",
        });
        assertContour(new String[]{
                "          ",
                " XXXXXXXX ",
                " XXXXXXXX ",
                "    XX    ",
                "    XX    ",
                "    XX    ",
        }, new String[]{
                "          ",
                " CIIIIIIC ",
                " CIIIIIIC ",
                "    II    ",
                "    II    ",
                "    CC    ",
        });
    }

    @Test
    public void testHoles(){
        assertContour(new String[]{
                "XXXXX",
                "XXXXX",
                "XX XX",
                "XXXXX",
                "XXXXX",
        }, new String[]{
                "IIIII",
                "IIIII",
                "II II",
                "IIIII",
                "IIIII",
        });
        assertContour(new String[]{
                "       ",
                " XXXXX ",
                " XXXXX ",
                " XX XX ",
                " XXXXX ",
                " XXXXX ",
                "       ",
        }, new String[]{
                "       ",
                " IIIII ",
                " IIIII ",
                " II II ",
                " IIIII ",
                " IIIII ",
                "       ",
        });

    }
    @Test
    public void testHoleSingleLine(){
        assertContour(new String[]{
                "XXX",
                "X X",
                "XXX",
        }, new String[]{
                "CIC",
                "I I",
                "CIC",
        });
        assertContour(new String[]{
                "     ",
                " XXX ",
                " X X ",
                " XXX ",
                "     ",
        }, new String[]{
                "     ",
                " CIC ",
                " I I ",
                " CIC ",
                "     ",
        });
        assertContour(new String[]{
                "XXXX",
                "X  X",
                "X  X",
                "XXXX",
        }, new String[]{
                "CIIC",
                "I  I",
                "I  I",
                "CIIC",
        });
        assertContour(new String[]{
                "XXXXX",
                "X   X",
                "X   X",
                "X   X",
                "XXXXX",
        }, new String[]{
                "CIIIC",
                "I   I",
                "I   I",
                "I   I",
                "CIIIC",
        });
    }
    @Test
    public void testShape(){
        assertContour(new String[]{
                "XXX    ",
                "XXXXXXX",
                "XXX    ",
        }, new String[]{
                "CII    ",
                "IIIIIIC",
                "CII    ",
        });
        assertContour(new String[]{
                "XXX  ",
                "XXXX ",
                "XXX  ",
                "XXXX ",
                "XXX  ",
                "XXXX ",
        }, new String[]{
                "CII  ",
                "IIIC ",
                "III  ",
                "IIIC ",
                "III  ",
                "CIII ",
        });
        assertContour(new String[]{
                "XXX  ",
                "XXXXX",
                "XXX  ",
                "XXXXX",
                "XXX  ",
                "XXXXX",
        }, new String[]{
                "CII  ",
                "IIIIC",
                "III  ",
                "IIIIC",
                "III  ",
                "CIIIC",
        });
        assertContour(new String[]{
                "XXXXX  ",
                "XXXXX  ",
                "XXXXXXX",
                "XXXXXXX",
                "XXXXXXX",
                "XXXXX  ",
                "XXXXX  ",
        }, new String[]{
                "CIIIC  ",
                "IIIII  ",
                "IIIICIC",
                "IIIIIII",
                "IIIICIC",
                "IIIII  ",
                "CIIIC  ",
        });
        assertContour(new String[]{
                "   XXXXX   ",
                "   XXXXX   ",
                "XXXXXXXXXXX",
                "XXXXXXXXXXX",
                "XXXXXXXXXXX",
                "   XXXXX   ",
                "   XXXXX   ",
        }, new String[]{
                "   CIIIC   ",
                "   IIIII   ",
                "CIICIIICIIC",
                "IIIIIIIIIII",
                "CIICIIICIIC",
                "   IIIII   ",
                "   CIIIC   ",
        });

    }

    @Test
    public void testRectangle(){
        assertContour(new String[]{
                "XXX",
                "XXX",
                "XXX",
        }, new String[]{
                "CIC",
                "III",
                "CIC",
        });
        assertContour(new String[]{
                "     ",
                " XXX ",
                " XXX ",
                " XXX ",
                "     "
        }, new String[]{
                "     ",
                " CIC ",
                " III ",
                " CIC ",
                "     "
        });
        assertContour(new String[]{
                "      ",
                " XXXX ",
                " XXXX ",
                " XXXX ",
                " XXXX ",
                "      "
        }, new String[]{
                "      ",
                " CIIC ",
                " IIII ",
                " IIII ",
                " CIIC ",
                "      "
        });
    }

    private void assertContour(String[] ground, String[] contour) {
        final TestHelper helper = new TestHelper();
        helper.setGround(ground);
        helper.run();

        helper.parse(new TestHelper.Runner() {
            @Override
            public char run(int x, int y, int z, char value) {
                if( value=='C' ) {
                    helper.map.getBlock(x,y,z).floor.setContour(x,z);
                }
                return 0;
            }
        }, contour);
        for (Floor floor : helper.map.floors) {
            floor.findContour();
            floor.findContour();
        }
        String[] actual = helper.evaluate(new TestHelper.Runner() {
            @Override
            public char run(int x, int y, int z, char value) {
                WalkableBlock block = helper.map.getBlock(x, y, z);
                if (block == null) {
                    return ' ';
                }
                if (block.floor.isContour(block)) {
                    return 'C';
                }
                return 'I';
            }
        });

        Assert.assertArrayEquals(contour, actual);
    }

}
