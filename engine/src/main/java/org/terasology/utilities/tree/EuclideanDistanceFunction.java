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
package org.terasology.utilities.tree;

public final class EuclideanDistanceFunction implements DistanceFunction {
    @Override
    public float getDistance(float[] p1, float[] p2) {
        float d = 0;

        int len = p1.length;
        for (int i = 0; i < len; i++) {
            float diff = (p1[i] - p2[i]);
            if (!Float.isNaN(diff)) {
                d += diff * diff;
            }
        }

        return (float) Math.sqrt(d);
    }

    @Override
    public float getDistanceLTE(float[] p1, float[] p2, float maxDistance) {
        final float maxDistanceSq = maxDistance * maxDistance;

        float d = 0;

        int len = p1.length;
        for (int i = 0; i < len; i++) {
            float diff = (p1[i] - p2[i]);
            if (!Float.isNaN(diff)) {
                d += diff * diff;
                if (d > maxDistanceSq)
                    return Float.NaN; //early termination, too far
            }
        }

        return (float) Math.sqrt(d);
    }

    @Override
    public boolean isDistanceZero(float[] p1, float[] p2) {

        int len = p1.length;
        for (int i = 0; i < len; i++) {
            float diff = (p1[i] - p2[i]);
            if (!Float.isNaN(diff) && diff!=0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public float getPointRegionDistance(float[] point, float[] min, float[] max) {
        float d = 0;

        for (int i = 0; i < point.length; i++) {
            float diff = 0;
            float pi = point[i];
            float maxi = max[i];
            if (pi > maxi) {
                diff = (pi - maxi);
            } else {
                float mini = min[i];
                if (pi < mini) {
                    diff = (pi - mini);
                }
            }

            if (!Float.isNaN(diff)) {
                d += diff * diff;
            }
        }

        return (float) Math.sqrt(d);
    }
}
