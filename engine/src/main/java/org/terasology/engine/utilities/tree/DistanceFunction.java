// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.tree;

public interface DistanceFunction {
    float getDistance(float[] p1, float[] p2);
    float getPointRegionDistance(float[] p1, float[] min, float[] max);
}
