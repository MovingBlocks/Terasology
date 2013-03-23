package org.terasology.world.generator.core;

/**
 * Climate simulation based on weighted distances
 *
 * @author Nym Traveel
 */
public class ClimateSimulator {
    private float[][] heightmap, climate, humidity;
    private int size;

    ClimateSimulator(float[][] hm) {
        heightmap = hm;
        size = heightmap.length;

        //Ready the Climate Map
        climate = new float[size][size];
        float[][] t1 = distanceFrom("poles", 10);
        float[][] t2 = distanceFrom("equator", 10);
        for (int width = 0; width < size; width++) {
            for (int height = 0; height < size; height++) {
                climate[width][height] = t1[width][height] + t2[width][height] - 1;
            }
        }
        overlayHeight(0, 0);

        //Ready the HumidityMap
        humidity = distanceFrom("water", 5);

    }

    private float[][] initDist(String fromWhat) {

        float[][] distArr = new float[size][size];
        if (fromWhat.equals("water")) {
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
        } else if (fromWhat.equals("poles")) {
            for (int width = 0; width < size; width++) {
                for (int height = 0; height < size; height++) {

                    if (height == 0) {  // topOfTheMap
                        distArr[height][width] = 0;
                    } else {
                        distArr[height][width] = size;
                    }
                }
            }
        } else if (fromWhat.equals("equator")) {
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
        return distArr;
    }

    private float[][] distanceFrom(String fromWhat, float heightInfluence) {

        float[][] distArr = initDist(fromWhat);
        float currentDistance = 0;

        System.out.println("Starting distance calculation: " + fromWhat);
        while (currentDistance < size) {
            for (int width = 0; width < size; width++) {
                for (int height = 0; height < size; height++) {
                    float currHeight = heightmap[width][height];
                    if (distArr[width][height] == size) { //Block could update
                        if (distArr[(width + 1) % size][height] + (heightmap[(width + 1) % size][height] - currHeight) * heightInfluence - currentDistance <= 0 ||
                                distArr[width][(height + 1) % size] + (heightmap[width][(height + 1) % size] - currHeight) * heightInfluence - currentDistance <= 0 ||
                                distArr[(width - 1 + size) % size][height] + (heightmap[(width - 1 + size) % size][height] - currHeight) * heightInfluence - currentDistance <= 0 ||
                                distArr[width][(height - 1 + size) % size] + (heightmap[width][(height - 1 + size) % size] - currHeight) * heightInfluence - currentDistance <= 0) {
                            //Updates over an edge
                            distArr[width][height] = currentDistance + 1;
                        } else if (
                                distArr[(width + 1) % size][(height + 1) % size] + (heightmap[(width + 1) % size][(height + 1) % size] - currHeight) * heightInfluence - (currentDistance + 0.41421) <= 0 ||
                                        distArr[(width - 1 + size) % size][(height + 1) % size] + (heightmap[(width - 1 + size) % size][(height + 1) % size] - currHeight) * heightInfluence - (currentDistance + 0.41421) <= 0 ||
                                        distArr[(width + 1) % size][(height - 1 + size) % size] + (heightmap[(width + 1) % size][(height - 1 + size) % size] - currHeight) * heightInfluence - (currentDistance + 0.41421) <= 0 ||
                                        distArr[(width - 1 + size) % size][(height - 1 + size) % size] + (heightmap[(width - 1 + size) % size][(height - 1 + size) % size] - currHeight) * heightInfluence - (currentDistance + 0.41421) <= 0) {
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

    private void overlayHeight(int strength, int locationInfluence) {
        for (int width = 0; width < size; width++) {
            for (int height = 0; height < size; height++) {
                float distToEq = (float) (0.5f - Math.abs(((height / 512.) * 2) - 1)) * 2;
                float heightFactor = heightmap[height][width] - 1;

                if (heightFactor < 0) {  // sea
                    climate[height][width] = distToEq * 0.4f;
                } else {                // land
                    climate[height][width] = ((100 - strength) * climate[height][width] + strength * ((distToEq * locationInfluence + (100 - locationInfluence) * 0.5f) * 0.01f - heightFactor * 0.05f)) * 0.01f;
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
