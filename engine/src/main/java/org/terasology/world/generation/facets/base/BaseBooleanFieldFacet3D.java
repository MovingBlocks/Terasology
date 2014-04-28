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
import org.terasology.math.Vector3i;

/**
 * @author Immortius
 */
public class BaseBooleanFieldFacet3D implements BooleanFieldFacet3D {

    private Vector3i size = new Vector3i();
    private boolean[] data;

    public BaseBooleanFieldFacet3D(Vector3i size) {
        this.size.set(size);
        data = new boolean[size.x * size.y * size.z];
    }

    @Override
    public boolean get(int x, int y, int z) {
        return data[x + size.getX() * (y + size.getY() * z)];
    }

    @Override
    public boolean get(Vector3i pos) {
        return get(pos.x, pos.y, pos.z);
    }

    @Override
    public boolean[] getInternal() {
        return data;
    }

    @Override
    public Boolean3DIterator get() {
        return new Boolean3DIterator() {
            private Vector3i position = new Vector3i(-1, 0, 0);
            private int nextIndex = 0;

            @Override
            public Vector3i currentPosition() {
                return position;
            }

            @Override
            public void setLast(boolean newValue) {
                data[nextIndex - 1] = newValue;
            }

            @Override
            public boolean next() {
                position.x++;
                if (position.x == size.x) {
                    position.x = 0;
                    position.y++;
                    if (position.y == size.y) {
                        position.y = 0;
                        position.z++;
                    }
                }
                return data[nextIndex++];
            }

            @Override
            public boolean hasNext() {
                return nextIndex < data.length;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void set(int x, int y, int z, boolean value) {
        data[x + size.getX() * (y + size.getY() * z)] = value;
    }

    @Override
    public void set(Vector3i pos, boolean value) {
        set(pos.x, pos.y, pos.z, value);
    }

    @Override
    public void set(boolean[] newData) {
        Preconditions.checkArgument(newData.length == data.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }

    @Override
    public void set(int index, boolean value) {
        data[index] = value;
    }
}
