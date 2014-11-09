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
