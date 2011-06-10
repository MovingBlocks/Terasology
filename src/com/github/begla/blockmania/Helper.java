/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.lwjgl.Sys;
import org.lwjgl.util.vector.Vector2f;

/**
 * A simple helper class for various tasks.
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Helper {

    /**
     * The logger used for managing and creating the default log file.
     */
    public static final Logger LOGGER = Logger.getLogger("blockmania");
    /* ---------- */
    private static final float _div = 1.0f / 16.0f;
    private static final long _timerTicksPerSecond = Sys.getTimerResolution();
    /* ---------- */
    private static Helper _instance = null;

    static {
        try {
            FileHandler fh = new FileHandler("blockmania.log", true);
            fh.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fh);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.toString(), ex);
        }
    }

    /**
     * Returns (and creates – if necessary) the static instance
     * of this helper class.
     *
     * @return The instance
     */
    public static Helper getInstance() {
        if (_instance == null) {
            _instance = new Helper();
        }


        return _instance;
    }

    /**
     * Calculates the texture offset for a given position within
     * the texture atlas.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @return The texture offset
     */
    public Vector2f calcOffsetForTextureAt(int x, int y) {
        return new Vector2f(x * _div, y * _div);
    }

    /**
     * Returns the system time in milliseconds.
     * 
     * @return The system time in milliseconds.
     */
    public long getTime() {
        return (Sys.getTime() * 1000) / _timerTicksPerSecond;
    }

    /**
     * Applies Cantor's pairing function to 2D coordinates.
     *
     * @param k1 X-coordinate
     * @param k2 Y-coordinate
     * @return Unique 1D value
     */
    public int cantorize(int k1, int k2) {
        return ((k1 + k2) * (k1 + k2 + 1) / 2) + k2;
    }

    /**
     * Inverse function of Cantor's pairing function.
     * 
     * @param c Cantor value
     * @return Value along the x-axis
     */
    public int cantorX(int c) {
        int j = (int) Math.floor(Math.sqrt(0.25 + 2 * c) - 0.5);
        return j - cantorY(c);
    }

    /**
     * Inverse function of Cantor's pairing function.
     * 
     * @param c Cantor value
     * @return Value along the y-axis
     */
    public int cantorY(int c) {
        int j = (int) Math.floor(Math.sqrt(0.25 + 2 * c) - 0.5);
        return c - j * (j + 1) / 2;
    }

    /**
     * Tests if a given position is within the bounds of a given 3D array.
     * 
     * @param x X-position
     * @param y Y-position
     * @param z Z-position
     * @param array The array
     * @return True if the position is within the bounds of the array
     */
    public boolean checkBounds3D(int x, int y, int z, byte[][][] array) {
        int length1 = array.length;

        if (x < 0 || x >= length1) {
            return false;
        }

        int length2 = array[x].length;

        if (y < 0 || y >= length2) {
            return false;
        }

        int length3 = array[x][y].length;

        if (z < 0 || z >= length3) {
            return false;
        }

        return true;
    }

    /**
     * Tests if a given position is within the bounds of a given 2D array.
     * 
     * @param x X-position
     * @param y Y-position
     * @param array The array
     * @return True if the position is within the bounds of the array
     */
    public boolean checkBounds2D(int x, int y, byte[][] array) {
        int length1 = array.length;
        int length2 = array[x].length;

        if (x < 0 || x >= length1) {
            return false;
        }

        if (y < 0 || y >= length2) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the flag at given byte position
     * is set.
     * 
     * @param value Byte value storing the flags
     * @param index Index position of the flag
     * @return True if the flag is set
     */
    public boolean isFlagSet(byte value, short index) {
        return (value & (1 << index)) != 0;
    }

    /**
     * Sets a flag at a given byte position.
     * 
     * @param value Byte value storing the flags
     * @param index Index position of the flag
     * @return The byte value containing the modified flag
     */
    public byte setFlag(byte value, short index) {
        return (byte) (value | (1 << index));
    }

    /**
     * 
     * @param x
     * @param y
     * @param q11
     * @param q12
     * @param q21
     * @param q22
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @return 
     */
    public float bilinearInterpolation(float x, float y, float q11, float q12, float q21, float q22, float x1, float x2, float y1, float y2) {
        float r1 = ((x2 - x) / (x2 - x1)) * q11 + ((x - x1) / (x2 - x1)) * q21;
        float r2 = ((x2 - x) / (x2 - x1)) * q12 + ((x - x1) / (x2 - x1)) * q22;
        float p = ((y2 - y) / (y2 - y1)) * r1 + ((y - y1) / (y2 - y1)) * r2;

        return p;
    }

    /*
     * 
     */
    public float linearInterolation(float x, float q11, float q12, float x1, float x2, float y1, float y2) {
        return y1 + (x - x1) * ((y2 - y1) / (x2 - x1));
    }
}
