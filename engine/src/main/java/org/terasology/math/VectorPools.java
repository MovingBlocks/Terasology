/*
 * Copyright 2018 MovingBlocks
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

import org.terasology.math.geom.Vector3i;

public class VectorPools {

    private static final int POOL_SIZE = 100_000; //Might be a bit overkill, but some might methods return array of all Vectors in chunk

    private static int currentVector3isAvailable;
    private static final Vector3i[] VECTOR_3I_POOL = new Vector3i[POOL_SIZE];

    private VectorPools() { }

    public static synchronized Vector3i getVector3i() {
        if (currentVector3isAvailable > 0) {
            VECTOR_3I_POOL[currentVector3isAvailable - 1].set(0, 0, 0);
            return VECTOR_3I_POOL[--currentVector3isAvailable];
        }
        return new Vector3i();
    }

    public static synchronized Vector3i getVector3i(Vector3i other) {
        if (currentVector3isAvailable > 0) {
            VECTOR_3I_POOL[currentVector3isAvailable - 1].set(other);
            return VECTOR_3I_POOL[--currentVector3isAvailable];
        }
        return new Vector3i(other);
    }

    public static synchronized Vector3i getVector3i(int x, int y, int z) {
        if (currentVector3isAvailable > 0) {
            VECTOR_3I_POOL[currentVector3isAvailable - 1].set(x, y, z);
            return VECTOR_3I_POOL[--currentVector3isAvailable];
        }
        return new Vector3i(x, y, z);
    }

    public static synchronized void free(Vector3i v) {
        if (currentVector3isAvailable < POOL_SIZE) {
            VECTOR_3I_POOL[currentVector3isAvailable++] = v;
        }
    }
}
