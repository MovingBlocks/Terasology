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

package org.terasology.core.world.generator;

/**
 * Climate simulation based on weighted distances
 *
 */
public class ClimateSimulator {
    private float[][] climate;
    private float[][] humidity;
    private ClimateSimulatorData climateDistanceData;

    ClimateSimulator(float[][] hm) {

        climateDistanceData = new ClimateSimulatorData(hm, hm.length);

        //Ready the Climate Map
        climate = new float[climateDistanceData.getSize()][climateDistanceData.getSize()];
        float[][] t1 = climateDistanceData.distanceFrom("poles", 10);
        float[][] t2 = climateDistanceData.distanceFrom("equator", 10);
        for (int width = 0; width < climateDistanceData.getSize(); width++) {
            for (int height = 0; height < climateDistanceData.getSize(); height++) {
                climate[width][height] = t1[width][height] + t2[width][height] - 1;
            }
        }
        overlayHeight(0, 0);

        //Ready the HumidityMap
        humidity = climateDistanceData.distanceFrom("water", 5);

    }

    private void overlayHeight(int strength, int locationInfluence) {
        for (int width = 0; width < climateDistanceData.getSize(); width++) {
            for (int height = 0; height < climateDistanceData.getSize(); height++) {
                float distToEq = (float) (0.5f - Math.abs(((height / 512.) * 2) - 1)) * 2;
                float heightFactor = climateDistanceData.getHeightmap()[height][width] - 1;

                if (heightFactor < 0) {  // sea
                    climate[height][width] = distToEq * 0.4f;
                } else {                // land
                    climate[height][width] = ((100 - strength) * climate[height][width]
                            + strength * ((distToEq * locationInfluence + (100 - locationInfluence) * 0.5f) * 0.01f - heightFactor * 0.05f)) * 0.01f;
                }
            }
        }
    }

    public float[][] getClimate() {
        return climate;
    }

    public float[][] getHumidity() {
        return humidity;
    }
}
