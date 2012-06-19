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
package org.terasology.model.structures;

import java.util.Arrays;

/**
 * A fast 3D array wrapper.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class TeraArray {

    private final byte _array[];
    private final int _lX, _lY, _lZ;
    private final int _size;

    /**
     * Init. a new 3D array with the given dimensions.
     */
    public TeraArray(int x, int y, int z) {
        _lX = x;
        _lY = y;
        _lZ = z;

        _size = _lX * _lY * _lZ;
        _array = new byte[_size];
    }

    public TeraArray(TeraArray other) {
        this._array = Arrays.copyOf(other._array, other._array.length);
        this._lX = other._lX;
        this._lY = other._lY;
        this._lZ = other._lZ;
        this._size = other._size;
    }

    /**
     * Returns the byte value at the given position.
     */
    public byte get(int x, int y, int z) {

        int pos = (x * _lX * _lY) + (y * _lX) + z;

        if (x >= _lX || y >= _lY || z >= _lZ || x < 0 || y < 0 || z < 0)
            return 0;

        return _array[pos];
    }

    /**
     * Sets the byte value for the given position.
     *
     * @param x
     * @param y
     * @param z
     * @param b
     * @return The old value at the given position
     */
    public byte set(int x, int y, int z, byte b) {
        int pos = (x * _lX * _lY) + (y * _lX) + z;

        if (x >= _lX || y >= _lY || z >= _lZ || x < 0 || y < 0 || z < 0)
            return 0;

        byte old = _array[pos];
        _array[pos] = b;
        return old;
    }

    /**
     * Sets the byte value for the given position.
     *
     * @param x
     * @param y
     * @param z
     * @param b
     * @return The old value at the given position
     */
    public boolean set(int x, int y, int z, byte b, byte expected) {
        int pos = (x * _lX * _lY) + (y * _lX) + z;

        if (x >= _lX || y >= _lY || z >= _lZ || x < 0 || y < 0 || z < 0)
            return false;

        byte old = _array[pos];
        if (old == expected) {
            _array[pos] = b;
            return true;
        }
        return false;
    }

    /**
     * Returns the raw byte at the given index.
     */
    public byte getRawByte(int i) {
        return _array[i];
    }

    /**
     * Sets the raw byte for the given index.
     */
    public void setRawByte(int i, byte b) {
        _array[i] = b;
    }

    /**
     * Returns the size of this array.
     */
    public int size() {
        return _size;
    }
}
