/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.world.chunks.deflate;

import com.google.common.base.Preconditions;
import org.terasology.world.chunks.blockdata.TeraArray;

/**
 * TeraVisitingDeflator uses the visitor pattern to gain access to the internal implementation details of specific
 * TeraArrays. This allows to implement fast deflation algorithms.
 *
 */
public abstract class TeraVisitingDeflator extends TeraDeflator {

    public TeraVisitingDeflator() {
    }

    @Override
    public final TeraArray deflate(TeraArray in) {
        TeraArray result = Preconditions.checkNotNull(in).deflate(this);
        if (result != null) {
            return result;
        }
        return in;
    }

    public abstract TeraArray deflateDenseArray16Bit(short[] data, int rowSize, int sizeX, int sizeY, int sizeZ);

    public abstract TeraArray deflateDenseArray8Bit(byte[] data, int rowSize, int sizeX, int sizeY, int sizeZ);

    public abstract TeraArray deflateDenseArray4Bit(byte[] data, int rowSize, int sizeX, int sizeY, int sizeZ);


    public abstract TeraArray deflateSparseArray16Bit(short[][] inflated, short[] deflated, short fill, int rowSize, int sizeX, int sizeY, int sizeZ);

    public abstract TeraArray deflateSparseArray8Bit(byte[][] inflated, byte[] deflated, byte fill, int rowSize, int sizeX, int sizeY, int sizeZ);

    public abstract TeraArray deflateSparseArray4Bit(byte[][] inflated, byte[] deflated, byte fill, int rowSize, int sizeX, int sizeY, int sizeZ);

}
