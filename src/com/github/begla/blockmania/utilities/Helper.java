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
package com.github.begla.blockmania.utilities;

/**
 * A simple helper class for various tasks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Helper {

    /**
     * Returns true if the flag at the given byte position
     * is set.
     *
     * @param value Byte value storing the flags
     * @param index Index position of the flag
     * @return True if the flag is set
     */
    public static boolean isFlagSet(byte value, short index) {
        return (value & (1 << index)) != 0;
    }

    /**
     * Sets a flag at a given byte position.
     *
     * @param value Byte value storing the flags
     * @param index Index position of the flag
     * @return The byte value containing the modified flag
     */
    public static byte setFlag(byte value, short index) {
        return (byte) (value | (1 << index));
    }
}
