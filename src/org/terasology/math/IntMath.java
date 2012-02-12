package org.terasology.math;

/**
 * Integer math utility class
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class IntMath {
    private IntMath() {
    }

    /**
     * Lowest power of two greater or equal to val
     * <p/>
     * For values &lt;= 0 returns 0
     *
     * @param val
     * @return The lowest power of two greater or equal to val
     */
    public static int ceilPowerOfTwo(int val) {
        val--;
        val = (val >> 1) | val;
        val = (val >> 2) | val;
        val = (val >> 4) | val;
        val = (val >> 8) | val;
        val = (val >> 16) | val;
        val++;
        return val;
    }

    /**
     * @param val
     * @return The size of a power of two - that is, the exponent.
     */
    public static int sizeOfPower(int val) {
        int power = 0;
        while (val > 1) {
            val = val >> 1;
            power++;
        }
        return power;
    }

    public static int floorToInt(float val) {
        return (int) Math.floor(val);
    }

    public static int ceilToInt(float val) {
        return (int) Math.ceil(val);
    }
}
