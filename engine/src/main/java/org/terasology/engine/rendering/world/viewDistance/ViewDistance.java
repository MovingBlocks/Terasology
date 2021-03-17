// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world.viewDistance;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.joml.Vector3i;
import org.joml.Vector3ic;

/**
 * The possible view distance options, that can be selected by the user.
 */
public enum ViewDistance {

    LEGALLY_BLIND("${engine:menu#view-distance-blind}", 0, new Vector3i(5, 5, 5)),
    NEAR("${engine:menu#view-distance-near}", 1, new Vector3i(9, 7, 9)),
    MODERATE("${engine:menu#view-distance-moderate}", 2, new Vector3i(13, 7, 13)),
    FAR("${engine:menu#view-distance-far}", 3, new Vector3i(17, 7, 17)),
    ULTRA("${engine:menu#view-distance-ultra}", 4, new Vector3i(25, 7, 25)),
    MEGA("${engine:menu#view-distance-mega}", 5, new Vector3i(33, 7, 33)),
    EXTREME("${engine:menu#view-distance-extreme}", 6, new Vector3i(63, 7, 63));

    private static TIntObjectMap<ViewDistance> indexLookup = new TIntObjectHashMap<>();

    private String displayName;
    private Vector3ic chunkDistance;
    private int index;

    static {
        for (ViewDistance viewDistance : ViewDistance.values()) {
            indexLookup.put(viewDistance.getIndex(), viewDistance);
        }
    }

    ViewDistance(String displayName, int index, Vector3i chunkDistance) {
        this.displayName = displayName;
        this.index = index;
        this.chunkDistance = chunkDistance;
    }

    public Vector3ic getChunkDistance() {
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
        return String.format("%1s (%2$dx%3$dx%4$d)", displayName, chunkDistance.x(), chunkDistance.y(), chunkDistance.z());
    }
}
