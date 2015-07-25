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
package org.terasology.rendering.world.viewDistance;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.terasology.math.geom.Vector3i;

/**
 * The possible view distance options, that can be selected by the user.
 */
public enum ViewDistance {

    LEGALLY_BLIND("Legally Blind", 0, new Vector3i(5, 5, 5)),
    NEAR("Near", 1, new Vector3i(9, 7, 9)),
    MODERATE("Moderate", 2, new Vector3i(13, 7, 13)),
    FAR("Far", 3, new Vector3i(17, 7, 17)),
    ULTRA("Ultra", 4, new Vector3i(25, 7, 25)),
    MEGA("Mega", 5, new Vector3i(33, 7, 33)),
    EXTREME("Extreme", 6, new Vector3i(63, 7, 63));

    private static TIntObjectMap<ViewDistance> indexLookup = new TIntObjectHashMap<>();

    private String displayName;
    private Vector3i chunkDistance;
    private int index;

    static {
        for (ViewDistance viewDistance : ViewDistance.values()) {
            indexLookup.put(viewDistance.getIndex(), viewDistance);
        }
    }

    private ViewDistance(String displayName, int index, Vector3i chunkDistance) {
        this.displayName = displayName;
        this.index = index;
        this.chunkDistance = chunkDistance;
    }

    public Vector3i getChunkDistance() {
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
