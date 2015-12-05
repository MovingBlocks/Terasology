/*
 * Copyright 2015 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates climate distance calculations for the climate simulator 
 * 
 * @author Michael
 *
 */
public class ClimateSimulatorData {
    private static final Logger logger = LoggerFactory.getLogger(ClimateSimulator.class);
    private float[][] heightmap;
    private int size;

    public ClimateSimulatorData(float[][] heightmap, int size) {
        this.heightmap = heightmap;
        this.size = size;
    }

    public float[][] getHeightmap() {
        return heightmap;
    }

    public int getSize() {
        return size;
    }

    public float[][] initDist(String fromWhat) {

        float[][] distArr = new float[size][size];
        switch(fromWhat)
        {
        case "water":
            distanceFromWater(distArr);
            break;
        case "poles":
            distanceFromPoles(distArr);
            break;
        case "equator":
            distanceFromEquator(distArr);
            break;
        }

        return distArr;
    }

    private void distanceFromWater(float[][] distArr) {
        for (int width = 0; width < size; width++) {
            for (int height = 0; height < size; height++) {
                float heightFactor = heightmap[height][width] - 1;

                if (heightFactor < 0) {  // sea
                    distArr[height][width] = 0;
                } else {  // land
                    distArr[height][width] = size;
                }
            }
        }
    }

    private void distanceFromPoles(float[][] distArr) {
        for (int width = 0; width < size; width++) {
            for (int height = 0; height < size; height++) {

                if (height == 0) {  // topOfTheMap
                    distArr[height][width] = 0;
                } else {
                    distArr[height][width] = size;
                }
            }
        }
    }

    private void distanceFromEquator(float[][] distArr) {
        for (int width = 0; width < size; width++) {
            for (int height = 0; height < size; height++) {

                if (height == size / 2) {  // topOfTheMap
                    distArr[height][width] = 0;
                } else {
                    distArr[height][width] = size;
                }
            }
        }
    }

    public float[][] distanceFrom(String fromWhat, float heightInfluence) {

        float[][] distArr = initDist(fromWhat);
        float currentDistance = 0;

        logger.info("Starting distance calculation: {}", fromWhat);
        while (currentDistance < size) {
            for (int width = 0; width < size; width++) {
                for (int height = 0; height < size; height++) {
                    float currHeight = heightmap[width][height];
                    if (distArr[width][height] == size) { //Block could update
                        int posW = (width + 1) % size;
                        int posH = (height + 1) % size;
                        int negW = ((width - 1) + size) % size;
                        int negH = (height - 1 + size) % size;

                        if (distArr[posW][height] + (heightmap[posW][height] - currHeight) * heightInfluence <= currentDistance
                                || distArr[width][posH] + (heightmap[width][posH] - currHeight) * heightInfluence <= currentDistance
                                || distArr[negW][height] + (heightmap[negW][height] - currHeight) * heightInfluence <= currentDistance
                                || distArr[width][negH] + (heightmap[width][negH] - currHeight) * heightInfluence <= currentDistance) {
                            //Updates over an edge
                            distArr[width][height] = currentDistance + 1;
                        } else if (
                                distArr[posW][posH] + (heightmap[posW][posH] - currHeight) * heightInfluence <= currentDistance + 0.41421
                                        || distArr[negW][posH] + (heightmap[negW][posH] - currHeight) * heightInfluence <= currentDistance + 0.41421
                                        || distArr[posW][negH] + (heightmap[posW][negH] - currHeight) * heightInfluence <= currentDistance + 0.41421
                                        || distArr[negW][negH] + (heightmap[negW][negH] - currHeight) * heightInfluence <= currentDistance + 0.41421) {
                            //Updates over the corner
                            distArr[width][height] = currentDistance + 1.41421f;
                        }
                    }
                }
            }
            currentDistance++;
        }

        //normalize Array
        float max = 0;
        for (int width = 0; width < size; width++) {
            for (int height = 0; height < size; height++) {
                max = distArr[width][height] > max ? distArr[width][height] : max;
            }
        }
        for (int width = 0; width < size; width++) {
            for (int height = 0; height < size; height++) {
                distArr[width][height] /= max;
            }
        }

        //invert if necessary
        if (fromWhat.equals("equator")) {
            for (int width = 0; width < size; width++) {
                for (int height = 0; height < size; height++) {
                    distArr[width][height] = 1 - distArr[width][height];
                }
            }
        }

        return distArr;
    }
}
