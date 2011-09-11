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
package com.github.begla.blockmania.rendering;

import javolution.util.FastList;
import org.lwjgl.util.vector.Vector3f;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class VectorPool {

    private static final FastList<Vector3f> _pool = new FastList<Vector3f>(512);

    public static Vector3f getVector(Vector3f v) {
        return getVector(v.x, v.y, v.z);
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static Vector3f getVector(double x, double y, double z) {

        Vector3f v = null;

        synchronized (_pool) {
            if (_pool.size() > 0) {
                v = _pool.removeFirst();
            }
        }

        if (v == null) {
            v = new Vector3f((float) x, (float) y, (float) z);
        } else {
            v.set((float) x, (float) y, (float) z);
        }

        return v;
    }

    /**
     * @return
     */
    public static Vector3f getVector() {
        synchronized (_pool) {
            return getVector(0f, 0f, 0f);
        }
    }

    /**
     * @param v
     */
    public static void putVector(Vector3f v) {
        synchronized (_pool) {
            if (_pool.size() < 512)
                _pool.add(v);
        }
    }
}
