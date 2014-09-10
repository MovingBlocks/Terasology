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

    LEGALLY_BLIND("Legally Blind", 0, 4),
    NEAR("Near", 1, 8),
    MODERATE("Moderate", 2, 16),
    FAR("Far", 3, 32),
    ULTRA("Ultra", 4, 64),
    MEGA("Mega", 5, 128),
    EXTREME("Extreme", 6, 512);

    private static TIntObjectMap<ViewDistance> indexLookup = new TIntObjectHashMap<>();

    private String displayName;
    private int chunkDistance;
    private int index;

    static {
        for (ViewDistance viewDistance : ViewDistance.values()) {
            indexLookup.put(viewDistance.getIndex(), viewDistance);
        }
    }

    private ViewDistance(String displayName, int index, int chunkDistance) {
        this.displayName = displayName;
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
            return LEGALLY_BLIND;
        }
        return result;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
