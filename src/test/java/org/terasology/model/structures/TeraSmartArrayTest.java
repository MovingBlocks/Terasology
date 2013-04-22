package org.terasology.model.structures;


import org.terasology.utilities.procedural.FastRandom;

public class TeraSmartArrayTest extends junit.framework.TestCase {

    private final byte[][][] _testArray = new byte[128][128][128];
    private final TeraSmartArray _array = new TeraSmartArray(128, 128, 128);

    public void testOrigin() throws Exception {
        _array.set(0, 0, 0, (byte) 15);
        assertEquals(15, _array.get(0, 0, 0));
        _array.set(0, 0, 0, (byte) 3);
        assertEquals(3, _array.get(0, 0, 0));
    }

    public void testSomeValues() throws Exception {
        _array.set(0, 2, 2, (byte) 15);
        assertEquals(15, _array.get(0, 2, 2));
        _array.set(122, 123, 4, (byte) 15);
        assertEquals(15, _array.get(122, 123, 4));
        _array.set(19, 4, 3, (byte) 15);
        assertEquals(15, _array.get(19, 4, 3));
        _array.set(120, 122, 123, (byte) 15);
        assertEquals(15, _array.get(120, 122, 123));
    }

    public void testNibbleBrothers() throws Exception {
        _array.set(0, 0, 0, (byte) 15);
        assertEquals(15, _array.get(0, 0, 0));
        _array.set(0, 0, 1, (byte) 12);
        assertEquals(12, _array.get(0, 0, 1));
        assertEquals(15, _array.get(0, 0, 0));
    }

    public void testAgainstArray() throws Exception {
        FastRandom rand = new FastRandom();

        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                for (int z = 0; z < 128; z++) {
                    int r = rand.randomInt();
                    r = (r < 0) ? -r : r;

                    _testArray[x][y][z] = (byte) (r % 16);
                    _array.set(x, y, z, _testArray[x][y][z]);
                }
            }
        }

        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                for (int z = 0; z < 128; z++) {
                    assertEquals(_testArray[x][y][z], _array.get(x, y, z));
                }
            }
        }
    }
}
