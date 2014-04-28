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

/**
 * @author Immortius
 */
public abstract class BaseFieldFacet2D implements FieldFacet2D {

    private Vector2i size = new Vector2i();
    private float[] data;

    public BaseFieldFacet2D(Vector2i size) {
        this.size.set(size);
        this.data = new float[size.getX() * size.getY()];
    }

    @Override
    public float get(int x, int y) {
        return data[x + size.getX() * y];
    }

    @Override
    public float get(Vector2i pos) {
        return data[pos.x + size.getX() * pos.y];
    }

    @Override
    public float[] getInternal() {
        return data;
    }

    @Override
    public Float2DIterator get() {
        return new Float2DIterator() {
            private Vector2i position = new Vector2i(-1, 0);
            private int index;

            @Override
            public Vector2i currentPosition() {
                return position;
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
    public void set(int x, int y, float value) {
        data[x + size.getX() * y] = value;
    }

    @Override
    public void set(Vector2i pos, float value) {
        data[pos.x + size.getX() * pos.y] = value;
    }

    @Override
    public void set(float[] newData) {
        Preconditions.checkArgument(newData.length == data.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }
}
