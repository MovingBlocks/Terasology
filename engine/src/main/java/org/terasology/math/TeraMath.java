/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.math;


import org.joml.Quaternionf;
import org.joml.Vector3i;

/**
 * Collection of math functions.
 */
public final class TeraMath {

    public static final float PI = (float) Math.PI;
    public static final float RAD_TO_DEG = (float) (180.0f / Math.PI);
    public static final float DEG_TO_RAD = (float) (Math.PI / 180.0f);

    private TeraMath() {
    }

    /**
     * a + b, but if the result exceeds Integer.MAX_VALUE then the result will be Integer.MAX_VALUE rather than overflowing.
     *
     * @param a
     * @param b
     * @return min(a + b, Integer.MAX_VALUE)
     */
    public static int addClampAtMax(int a, int b) {
        long result = (long) a + (long) b;
        return (int) Math.min(result, Integer.MAX_VALUE);
    }

    /**
     * Returns the absolute value.
     *
     * @param i
     * @return the absolute value
     */
    public static int fastAbs(int i) {
        return (i >= 0) ? i : -i;
    }

    /**
     * Returns the absolute value (float variant)
     *
     * @param d
     * @return the absolute value
     */
    public static float fastAbs(float d) {
        return (d >= 0) ? d : -d;
    }

    /**
     * Returns the absolute value (double variant).
     *
     * @param d
     * @return the absolute value of d
     */
    public static double fastAbs(double d) {
        return (d >= 0) ? d : -d;
    }

    /**
     * Fast floor function
     *
     * @param d
     * @return
     */
    public static double fastFloor(double d) {
        int i = (int) d;
        return (d < 0 && d != i) ? i - 1 : i;
    }

    /**
     * Fast floor function
     *
     * @param d
     * @return
     */
    public static float fastFloor(float d) {
        int i = (int) d;
        return (d < 0 && d != i) ? i - 1 : i;
    }

    /**
     * Clamps a given value to be an element of [0..1].
     *
     * @param value
     * @return
     */
    public static double clamp(double value) {
        if (value > 1.0) {
            return 1.0;
        } else if (value < 0.0) {
            return 0.0;
        }
        return value;
    }

    /**
     * Clamps a given value to be an element of [0..1].
     *
     * @param value
     * @return
     */
    public static float clamp(float value) {
        if (value > 1.0f) {
            return 1.0f;
        } else if (value < 0.0f) {
            return 0.0f;
        }
        return value;
    }

    /**
     * Clamps a given value to be an element of [min..max].
     *
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static double clamp(double value, double min, double max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }
        return value;
    }

    /**
     * Clamps a given value to be an element of [min..max].
     *
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static float clamp(float value, float min, float max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }
        return value;
    }

    /**
     * Clamps a given value to be an element of [min..max].
     *
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static int clamp(int value, int min, int max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }
        return value;
    }

    /**
     * Checks if given float is finite
     *
     * @param value
     * @return
     */
    public static boolean isFinite(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }

    /**
     * Checks if given double is finite
     *
     * @param value
     * @return
     */
    public static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    /**
     * Fast power function
     *
     * @param base
     * @param exp
     * @return
     */
    public static int pow(int base, int exp) {
        switch (exp) {
            case 0:
                return 1;
            case 1:
                return base;
            case 2:
                return base * base;
            case 3:
                return base * base * base;
        }
        if (exp < 0) {
            if (base == 0) {
                throw new ArithmeticException("0^" + exp + " causes division by zero");
            }
            if (base == 1) {
                return 1;
            }
            if (base == -1) {
                if (exp % 2 == 0) {
                    return 1;
                }
                return -1;
            }
            return 0;
        }
        int temp = base;
        exp--;
        while (true) {
            if ((exp & 1) != 0) {
                base *= temp;
            }
            exp >>= 1;
            if (exp <= 0) {
                break;
            }
            temp *= temp;
        }
        return base;
    }

    /**
     * Fast power function
     *
     * @param base
     * @param exp
     * @return
     */
    public static long pow(long base, int exp) {
        switch (exp) {
            case 0:
                return 1L;
            case 1:
                return base;
            case 2:
                return base * base;
            case 3:
                return base * base * base;
        }
        if (exp < 0) {
            if (base == 0L) {
                throw new ArithmeticException("0^" + exp + " causes division by zero");
            }
            if (base == 1L) {
                return 1L;
            }
            if (base == -1L) {
                if (exp % 2 == 0) {
                    return 1L;
                }
                return -1L;
            }
            return 0L;
        }
        long temp = base;
        exp--;
        while (true) {
            if ((exp & 1) != 0) {
                base *= temp;
            }
            exp >>= 1;
            if (exp == 0) {
                break;
            }
            temp *= temp;
        }
        return base;
    }

    /**
     * Fast power function
     *
     * @param base
     * @param exp
     * @return
     */
    public static float pow(float base, int exp) {
        if (exp <= 0) {
            if (exp == 0) {
                // 0^0 is an indetermination, and should therefore return NaN, but Java Math.pow does return 1.0,
                // so this function will also return 1.0
                return 1.0f;
            }
            base = 1.0f / base;
            exp = -exp;
        }
        float temp = base;
        exp--;
        while (true) {
            if ((exp & 1) != 0) {
                base *= temp;
            }
            exp >>= 1;
            if (exp == 0) {
                break;
            }
            temp *= temp;
        }
        return base;
    }

    /**
     * Fast power function
     *
     * @param base
     * @param exp
     * @return
     */
    public static double pow(double base, int exp) {
        if (exp <= 0) {
            if (exp == 0) {
                return 1.0;
            }
            base = 1.0 / base;
            exp = -exp;
        }
        double temp = base;
        exp--;
        while (true) {
            if ((exp & 1) != 0) {
                base *= temp;
            }
            exp >>= 1;
            if (exp == 0) {
                break;
            }
            temp *= temp;
        }
        return base;
    }


    /**
     * Modulus operation, where the result has the same sign as the divisor.
     * <br><br>
     * Modulus(a, b) differs from a % b in that the result of the first has the
     * same sign as a, while the latter has the same sign as b.
     *
     * @param dividend The value that is divided
     * @param divisor  The value with which is divided
     * @return The remainder of (dividend / divisor) as a number in the range [0, divisor)
     * @author DizzyDragon
     */
    public static double modulus(double dividend, double divisor) {
        return ((dividend % divisor) + divisor) % divisor;
    }

    /**
     * Modulus operation, where the result has the same sign as the divisor.
     * <br><br>
     * Modulus(a, b) differs from a % b in that the result of the first has the
     * same sign as a, while the latter has the same sign as b.
     *
     * @param dividend The value that is divided
     * @param divisor  The value with which is divided
     * @return The remainder of (dividend / divisor) as a number in the range [0, divisor)
     * @author DizzyDragon
     */
    public static float modulus(float dividend, float divisor) {
        return ((dividend % divisor) + divisor) % divisor;
    }

    /**
     * Modulus operation, where the result has the same sign as the dividend.
     * <br><br>
     * Modulus(a, b) equals a % b.
     * This function (alias) exists primarily to be used in places where both modulus and % are used,
     * to make a clearer distinction between the two operations.
     *
     * @param dividend The value that is divided
     * @param divisor  The value with which is divided
     * @return The remainder of (dividend / divisor) as a number in the range [0, divisor)
     * @author DizzyDragon
     */
    public static double remainder(double dividend, double divisor) {
        return dividend % divisor;
    }

    /**
     * Modulus operation, where the result has the same sign as the dividend.
     * <br><br>
     * Modulus(a, b) equals a % b.
     * This function (alias) exists primarily to be used in places where both modulus and % are used,
     * to make a clearer distinction between the two operations.
     *
     * @param dividend The value that is divided
     * @param divisor  The value with which is divided
     * @return The remainder of (dividend / divisor) as a number in the range [0, divisor)
     * @author DizzyDragon
     */
    public static float remainder(float dividend, float divisor) {
        return dividend % divisor;
    }

    /**
     * Bilinear interpolation.
     */
    public static double biLerp(double x, double y, double q11, double q12, double q21, double q22, double x1, double x2, double y1, double y2) {
        double r1 = lerp(x, x1, x2, q11, q21);
        double r2 = lerp(x, x1, x2, q12, q22);
        return lerp(y, y1, y2, r1, r2);
    }

    /**
     * Linear interpolation.
     */
    public static double lerp(double t, double x1, double x2, double q00, double q01) {
        return ((x2 - t) / (x2 - x1)) * q00 + ((t - x1) / (x2 - x1)) * q01;
    }

    public static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    public static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    public static float biLerp(float q00, float q10, float q01, float q11, float tx, float ty) {
        float lerpX1 = lerp(q00, q10, tx);
        float lerpX2 = lerp(q01, q11, tx);
        return TeraMath.lerp(lerpX1, lerpX2, ty);
    }

    public static double biLerp(double q00, double q10, double q01, double q11, double tx, double ty) {
        double lerpX1 = lerp(q00, q10, tx);
        double lerpX2 = lerp(q01, q11, tx);
        return TeraMath.lerp(lerpX1, lerpX2, ty);
    }

    public static float triLerp(float q000, float q100, float q010, float q110, float q001, float q101, float q011, float q111, float tx, float ty, float tz) {
        float x00 = lerp(q000, q100, tx);
        float x10 = lerp(q010, q110, tx);
        float x01 = lerp(q001, q101, tx);
        float x11 = lerp(q011, q111, tx);
        float y0 = lerp(x00, x10, ty);
        float y1 = lerp(x01, x11, ty);
        return lerp(y0, y1, tz);
    }

    public static double triLerp(double q000, double q100, double q010, double q110, double q001, double q101, double q011, double q111, double tx, double ty, double tz) {
        double x00 = lerp(q000, q100, tx);
        double x10 = lerp(q010, q110, tx);
        double x01 = lerp(q001, q101, tx);
        double x11 = lerp(q011, q111, tx);
        double y0 = lerp(x00, x10, ty);
        double y1 = lerp(x01, x11, ty);
        return lerp(y0, y1, tz);
    }

    /**
     * Trilinear interpolation.
     */
    public static double triLerp(double x, double y, double z, double q000, double q001, double q010, double q011, double q100, double q101, double q110, double q111,
                                 double x1, double x2, double y1, double y2, double z1, double z2) {
        double x00 = lerp(x, x1, x2, q000, q100);
        double x10 = lerp(x, x1, x2, q010, q110);
        double x01 = lerp(x, x1, x2, q001, q101);
        double x11 = lerp(x, x1, x2, q011, q111);
        double r0 = lerp(y, y1, y2, x00, x01);
        double r1 = lerp(y, y1, y2, x10, x11);
        return lerp(z, z1, z2, r0, r1);
    }

    /**
     * Lowest power of two greater or equal to val
     * <br><br>
     * For values &lt;= 0 returns 0
     * For values &gt;= 2 ^ 30 returns 0. (2^30 is the largest power of 2 that fits within a int).
     *
     * @param val
     * @return The lowest power of two greater or equal to val
     */
    public static int ceilPowerOfTwo(int val) {
        int result = val - 1;
        result = (result >> 1) | result;
        result = (result >> 2) | result;
        result = (result >> 4) | result;
        result = (result >> 8) | result;
        result = (result >> 16) | result;
        result++;

        return (result & ~(Integer.MIN_VALUE));
    }

    /**
     * @param val
     * @return Whether val is a power of two
     * @deprecated Use com.google.common.math.IntMath.isPowerOfTwo instead
     */
    @Deprecated
    public static boolean isPowerOfTwo(int val) {
        return com.google.common.math.IntMath.isPowerOfTwo(val);
    }

    /**
     * @param value
     * @return The size of a power of two - that is, the exponent.
     */
    public static int sizeOfPower(int value) {
        int power = 0;
        int val = value;
        while (val > 1) {
            val = val >> 1;
            power++;
        }
        return power;
    }

    /**
     * Perlin's blending spline (interpolation function)
     * <p>
     * 6t<sup>5</sup>-15t<sup>4</sup>+10t<sup>3</sup>
     * </p>
     * It has both 1st and 2nd derivative of 0 at 0 and 1
     *
     * @param t
     */
    public static float fadePerlin(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /**
     * Hermite's blending spline h01 (interpolation function)
     * <p>
     * 3t<sup>2</sup>-2t<sup>3</sup>
     * </p>
     * It has a 1st derivative of 0 at 0 and 1
     *
     * @param t
     */
    public static float fadeHermite(float t) {
        return t * t * (3 - 2 * t);
    }


    public static int floorToInt(float val) {
        int i = (int) val;
        return (val < 0 && val != i) ? i - 1 : i;
    }

    public static int floorToInt(double val) {
        int i = (int) val;
        return (val < 0 && val != i) ? i - 1 : i;
    }

    public static int ceilToInt(float val) {
        int i = (int) val;
        return (val >= 0 && val != i) ? i + 1 : i;
    }

    public static int ceilToInt(double val) {
        int i = (int) val;
        return (val >= 0 && val != i) ? i + 1 : i;
    }

    /**
     * @param yaw the yaw angle (in radians)
     * @param pitch the pitch angle (in radians)
     * @param roll the roll angle (in radians)
     */
    public static Quaternionf toYawPitchRoll(float yaw, float pitch, float roll) {
        float halfYaw = yaw * 0.5f;
        float halfPitch = pitch * 0.5f;
        float halfRoll = roll * 0.5f;
        float cosYaw = (float) (Math.cos(halfYaw));
        float sinYaw = (float) (Math.sin(halfYaw));
        float cosPitch = (float) (Math.cos(halfPitch));
        float sinPitch = (float) (Math.sin(halfPitch));
        float cosRoll = (float) (Math.cos(halfRoll));
        float sinRoll = (float) (Math.sin(halfRoll));
        return new Quaternionf().set(
            cosRoll * sinPitch * cosYaw + sinRoll * cosPitch * sinYaw,
            cosRoll * cosPitch * sinYaw - sinRoll * sinPitch * cosYaw,
            sinRoll * cosPitch * cosYaw - cosRoll * sinPitch * sinYaw,
            cosRoll * cosPitch * cosYaw + sinRoll * sinPitch * sinYaw
        );
    }

    /**
     * Rotates a given 2D array by 90 degrees
     * @param array an array with equal size in both dimensions
     * @return a new, rotated array
     */
    public static float[][] rotateArray(float[][] array) {
        int size = array.length;
        float[][] newArray = new float[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                newArray[i][j] = array[j][size - i - 1];
            }
        }
        return newArray;
    }

    /**
     * Shifts all elements given 2D array by given offsets (wraps around)
     * @param array an array with equal size in both dimensions
     * @param x the offset in x direction
     * @param y the offset in y direction
     * @return a new, shifted array
     */
    public static float[][] shiftArray(float[][] array, int x, int y) {
        int size = array.length;
        float[][] newArray = new float[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                newArray[i][j] = array[(i + x + size) % size][(j + y + size) % size];
            }
        }
        return newArray;
    }

    public static int calculate3DArrayIndex(Vector3i pos, Vector3i size) {
        return calculate3DArrayIndex(pos.x, pos.y, pos.z, size);
    }

    public static int calculate3DArrayIndex(int x, int y, int z, Vector3i size) {
        return x + size.x * (z + size.z * (y));
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt(value);
    }

    public static float sqr(float f) {
        return f * f;
    }

    public static float sqr(int i) {
        return i * i;
    }

    /**
     * Returns the floating-point value adjacent to {@code d} in the direction of negative infinity.
     * @param f  starting floating-point value
     * @return The adjacent floating-point value closer to negative infinity.
     * @deprecated Use {@link Math#nextDown(float)} if Java 8 is available.
     */
    @Deprecated
    public static float nextDown(float f) {
        if (Float.isNaN(f) || f == Float.NEGATIVE_INFINITY) {
            return f;
        } else {
            if (f == 0.0f) {
                return -Float.MIN_VALUE;
            } else {
                return Float.intBitsToFloat(Float.floatToRawIntBits(f) + ((f > 0.0f) ? -1 : 1));
            }
        }
    }

    /**
     * Returns the floating-point value adjacent to {@code f} in the direction of positive infinity.
     * @param f starting floating-point value
     * @return The adjacent floating-point value closer to positive infinity.
     * @deprecated Use {@link Math#nextUp(float)} if Java 8 is available.
     */
    @Deprecated
    public static float nextUp(float f) {
        if (Float.isNaN(f) || f == Float.POSITIVE_INFINITY) {
            return f;
        } else {
            f += 0.0f;
            return Float.intBitsToFloat(Float.floatToRawIntBits(f) + ((f >= 0.0f) ? 1 : -1));
        }
    }

}
