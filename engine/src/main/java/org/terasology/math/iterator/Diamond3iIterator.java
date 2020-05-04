/*
 * Copyright 2020 MovingBlocks
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

package org.terasology.math.iterator;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.math.TeraMath;

import java.util.Iterator;

/**
 */
public final class Diamond3iIterator implements Iterable<Vector3ic> {

    private final Vector3i origin = new Vector3i();
    private final int endDistance;
    private int startDistance = 0;

    private Diamond3iIterator(Vector3ic origin, int maxDistance) {
        this.origin.set(origin);
        this.endDistance = maxDistance;
    }

    private Diamond3iIterator(Vector3ic origin, int startDistance, int maxDistance) {
        this.origin.set(origin);
        this.endDistance = maxDistance;
        this.startDistance = startDistance;
    }

    public static Diamond3iIterator.Builder region(Vector3ic origin, int radius) {
        return new Diamond3iIterator.Builder(origin, radius);
    }

    public static Diamond3iIterator.Builder hollow(Vector3ic origin, int start, int end) {
        return new Diamond3iIterator.Builder(origin, end).start(start);
    }

    public static Diamond3iIterator.Builder shell(Vector3ic origin, int radius) {
        return new Diamond3iIterator.Builder(origin, radius).start(radius - 1);
    }

    @Override
    public Iterator<Vector3ic> iterator() {
        Vector3i pos = new Vector3i();
        final int[] level = {this.startDistance};
        final int[] offset = {-this.startDistance,0,0};

        return new Iterator<Vector3ic>() {
            @Override
            public boolean hasNext() {
                return level[0] < endDistance;
            }

            @Override
            public Vector3ic next() {
                pos.set(origin.x + offset[0], origin.y + offset[1], origin.z + offset[2]);
                if (offset[2] < 0) {
                    offset[2] *= -1;
                } else if (offset[1] < 0) {
                    offset[1] *= -1;
                    offset[2] = -(level[0] - TeraMath.fastAbs(offset[0]) - TeraMath.fastAbs(offset[1]));
                } else {
                    offset[1] = -offset[1] + 1;
                    if (offset[1] > 0) {
                        if (++offset[0] <= level[0]) {
                            offset[1] = TeraMath.fastAbs(offset[0]) - level[0];
                            offset[2] = 0;
                        } else {
                            level[0]++;
                            offset[0] = -level[0];
                            offset[1] = 0;
                            offset[2] = 0;
                        }
                    } else {
                        offset[2] = -(level[0] - TeraMath.fastAbs(offset[0]) - TeraMath.fastAbs(offset[1]));
                    }
                }
                return pos;
            }
        };
    }

    public static final class Builder {
        private final Vector3ic origin;
        private final int endDistance;
        private int startDistance = 0;

        private Builder(Vector3ic origin, int endDistance){
            this.origin = origin;
            this.endDistance = endDistance + 1;
        }

        public Diamond3iIterator.Builder start(int start) {
            this.startDistance = start;
            return this;
        }

        public Diamond3iIterator build() {
            return new Diamond3iIterator(origin,startDistance,endDistance);
        }

    }

}
