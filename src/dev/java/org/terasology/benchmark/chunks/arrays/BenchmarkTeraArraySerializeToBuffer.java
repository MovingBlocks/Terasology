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
package org.terasology.benchmark.chunks.arrays;

import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraArray.SerializationHandler;

import java.nio.ByteBuffer;

@SuppressWarnings("rawtypes")
public class BenchmarkTeraArraySerializeToBuffer extends BenchmarkTeraArraySerialization {

    protected ByteBuffer buffer;

    public BenchmarkTeraArraySerializeToBuffer(SerializationHandler handler, TeraArray array) {
        super(handler, array);
    }

    @Override
    public String getTitle() {
        return array.getClass().getSimpleName() + " serialization directly into ByteBuffer";
    }

    @Override
    public void setup() {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    @Override
    public void prerun() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        int bogus = 0;
        handler.serialize(array, buffer);
        bogus += buffer.position();
        buffer.rewind();
    }

    @Override
    public void postrun() {
    }

    @Override
    public void finish(boolean aborted) {
    }
}
