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
package org.terasology.rendering.world;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author Immortius
 */
public enum ViewDistance {
    NEAR(0, 8),
    MODERATE(1, 16),
    FAR(2, 32),
    ULTRA(3, 64);

    private int chunkDistance;
    private int index;

    private static TIntObjectMap<ViewDistance> indexLookup = new TIntObjectHashMap<>();

    static {
        for (ViewDistance viewDistance : ViewDistance.values()) {
            indexLookup.put(viewDistance.getIndex(), viewDistance);
        }
    }

    private ViewDistance(int index, int chunkDistance) {
        this.index = index;
        this.chunkDistance = chunkDistance;
    }

    public int getChunkDistance() {
        return chunkDistance;
    }

    public int getIndex() {
        return index;
    }

    public static ViewDistance forIndex(int viewDistanceLevel) {
        ViewDistance result = indexLookup.get(viewDistanceLevel);
        if (result == null) {
            return NEAR;
        }
        return result;
    }
}
