/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.generation.facets.base;

import com.google.common.base.Preconditions;
import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;

/**
 * @author Immortius
 */
public abstract class BaseFieldFacet3D implements FieldFacet3D {

    private Vector3i size = new Vector3i();
    private float[] data;

    public BaseFieldFacet3D(Vector3i size) {
        this.size.set(size);
        this.data = new float[size.getX() * size.getY() * size.getZ()];
    }

    @Override
    public float get(int x, int y, int z) {
        return data[x + size.getX() * (y + size.getY() * z)];
    }

    @Override
    public float get(Vector3i pos) {
        return get(pos.x, pos.y, pos.z);
    }

    @Override
    public float[] getInternal() {
        return data;
    }

    @Override
    public Float3DIterator get() {
        return new Float3DIterator() {
            private Vector3i position = new Vector3i(-1, 0, 0);
            private int index;

            @Override
            public Vector3i currentPosition() {
                return position;
            }

            @Override
            public Vector2i current2DPos() {
                return new Vector2i(position.getX(), position.getZ());
            }

            @Override
            public void setLast(float newValue) {
                data[index - 1] = newValue;
            }

            @Override
            public float next() {
                position.x++;
                if (position.x == size.x) {
                    position.x = 0;
                    position.y++;
                    if (position.y == size.y) {
                        position.y = 0;
                        position.z++;
                    }
                }
                return data[index++];
            }

            @Override
            public boolean hasNext() {
                return index < data.length;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void set(int x, int y, int z, float value) {
        data[x + size.getX() * (y + size.getY() * z)] = value;
    }

    @Override
    public void set(Vector3i pos, float value) {
        set(pos.x, pos.y, pos.z, value);
    }

    @Override
    public void set(float[] newData) {
        Preconditions.checkArgument(newData.length == data.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }
}
