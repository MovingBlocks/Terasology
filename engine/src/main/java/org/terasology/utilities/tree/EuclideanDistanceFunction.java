// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.tree;

public class EuclideanDistanceFunction implements DistanceFunction {
    @Override
    public float getDistance(float[] p1, float[] p2) {
        float d = 0;

        for (int i = 0; i < p1.length; i++) {
            float diff = (p1[i] - p2[i]);
            if (!Float.isNaN(diff)) {
                d += diff * diff;
            }
        }

        return (float) Math.sqrt(d);
    }

    @Override
    public float getPointRegionDistance(float[] point, float[] min, float[] max) {
        float d = 0;

        for (int i = 0; i < point.length; i++) {
            float diff = 0;
            if (point[i] > max[i]) {
                diff = (point[i] - max[i]);
            } else if (point[i] < min[i]) {
                diff = (point[i] - min[i]);
            }

            if (!Float.isNaN(diff)) {
                d += diff * diff;
            }
        }

        return (float) Math.sqrt(d);
    }
}
