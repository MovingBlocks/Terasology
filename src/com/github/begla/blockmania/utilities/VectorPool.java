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

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class VectorPool {

    static ArrayBlockingQueue<Vector3f> _pool = new ArrayBlockingQueue<Vector3f>(512);

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static Vector3f getVector(float x, float y, float z) {
        Vector3f v = _pool.poll();

        if (v == null) {
            v = new Vector3f(x, y, z);
        } else {
            v.set(x, y, z);
        }

        return v;
    }

    /**
     * 
     * @return
     */
    public static Vector3f getVector() {
        return getVector(0f, 0f, 0f);
    }

    /**
     * 
     * @param v
     */
    public static void putVector(Vector3f v) {
        if (_pool.size() < 512) {
            _pool.add(v);
        }
    }
}
