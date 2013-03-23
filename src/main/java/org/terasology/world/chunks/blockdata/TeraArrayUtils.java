/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.world.chunks.blockdata;

/**
 * TeraArrayUtils contains some methods used in some TeraArray implementations.
 *
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 */
public final class TeraArrayUtils {

    private TeraArrayUtils() {
    }

    public static final byte getLo(int value) {
        return (byte) (value & 0x0F);
    }

    public static final byte getHi(int value) {
        return (byte) ((value & 0xF0) >> 4);
    }

    public static final byte setHi(int value, int hi) {
        return makeByte(hi, getLo(value));
    }

    public static final byte setLo(int value, int lo) {
        return makeByte(getHi(value), lo);
    }

    public static final byte makeByte(int hi, int lo) {
        return (byte) ((hi << 4) | (lo));
    }
}
