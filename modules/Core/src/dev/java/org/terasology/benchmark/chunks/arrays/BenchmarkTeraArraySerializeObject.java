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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class BenchmarkTeraArraySerializeObject extends BenchmarkTeraArraySerialization {

    protected ByteArrayOutputStream out;

    @SuppressWarnings("rawtypes")
    public BenchmarkTeraArraySerializeObject(SerializationHandler handler, TeraArray array) {
        super(handler, array);
    }

    @Override
    public String getTitle() {
        return array.getClass().getSimpleName() + " builtin object serialization directly into ByteArrayOutputStream";
    }

    @Override
    public void setup() {
        out = new ByteArrayOutputStream(BUFFER_SIZE);
    }

    @Override
    public void prerun() {
    }

    @Override
    public void run() {
        try {
            (new ObjectOutputStream(out)).writeObject(array);
            out.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postrun() {
    }

    @Override
    public void finish(boolean aborted) {
    }
}
