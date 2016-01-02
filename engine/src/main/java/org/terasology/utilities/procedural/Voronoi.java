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
package org.terasology.utilities.procedural;

import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2f;

import java.util.Random;

/**
 */
public class Voronoi {

    private static final float DENSITY_ADJUSTMENT = 0.39815f;
    private static final float INVERSE_DENSITY_ADJUSTMENT = 1.0f / DENSITY_ADJUSTMENT;

    private Vector2f offset;

    private int[] poissonCount = new int[]{
            4, 3, 1, 1, 1, 2, 4, 2, 2, 2, 5, 1, 0, 2, 1, 2, 2, 0, 4, 3, 2, 1, 2, 1, 3, 2, 2, 4, 2, 2, 5, 1, 2, 3,
            2, 2, 2, 2, 2, 3, 2, 4, 2, 5, 3, 2, 2, 2, 5, 3, 3, 5, 2, 1, 3, 3, 4, 4, 2, 3, 0, 4, 2, 2, 2, 1, 3, 2,
            2, 2, 3, 3, 3, 1, 2, 0, 2, 1, 1, 2, 2, 2, 2, 5, 3, 2, 3, 2, 3, 2, 2, 1, 0, 2, 1, 1, 2, 1, 2, 2, 1, 3,
            4, 2, 2, 2, 5, 4, 2, 4, 2, 2, 5, 4, 3, 2, 2, 5, 4, 3, 3, 3, 5, 2, 2, 2, 2, 2, 3, 1, 1, 4, 2, 1, 3, 3,
            4, 3, 2, 4, 3, 3, 3, 4, 5, 1, 4, 2, 4, 3, 1, 2, 3, 5, 3, 2, 1, 3, 1, 3, 3, 3, 2, 3, 1, 5, 5, 4, 2, 2,
            4, 1, 3, 4, 1, 5, 3, 3, 5, 3, 4, 3, 2, 2, 1, 1, 1, 1, 1, 2, 4, 5, 4, 5, 4, 2, 1, 5, 1, 1, 2, 3, 3, 3,
            2, 5, 2, 3, 3, 2, 0, 2, 1, 1, 4, 2, 1, 3, 2, 1, 2, 2, 3, 2, 5, 5, 3, 4, 5, 5, 2, 4, 4, 5, 3, 2, 2, 2,
            1, 4, 2, 3, 3, 4, 2, 5, 4, 2, 4, 2, 2, 2, 4, 5, 3, 2
    };


    public Voronoi(Random random) {
        offset = new Vector2f(99999 * random.nextFloat(), 99999 * random.nextFloat());
    }

    public static float standardDistanceFunction(Vector2f delta) {
        return delta.x * delta.x + delta.y * delta.y;
    }

    /**
     * @param at
     * @param numPoints Should be &le; 5. The number of points to return
     * @return
     */
    public VoronoiResult[] getClosestPoints(Vector2f at, int numPoints) {
        VoronoiResult[] results = new VoronoiResult[numPoints];
        for (VoronoiResult result : results) {
            result.distance = Float.MAX_VALUE;
        }

        at.scale(DENSITY_ADJUSTMENT);
        at.add(offset);

        int cellX = TeraMath.floorToInt(at.x);
        int cellY = TeraMath.floorToInt(at.y);

        processCell(cellX, cellY, at, results);

        Vector2f cellPos = new Vector2f(at);
        cellPos.x -= cellX;
        cellPos.y -= cellY;
        Vector2f distMax = new Vector2f(standardDistanceFunction(new Vector2f(1 - cellPos.x, 0)), standardDistanceFunction(new Vector2f(0, 1 - cellPos.y)));
        Vector2f distMin = new Vector2f(standardDistanceFunction(new Vector2f(cellPos.x, 0)), standardDistanceFunction(new Vector2f(0, cellPos.y)));

        // Test near cells
        if (distMin.x < results[results.length - 1].distance) {
            processCell(cellX - 1, cellY, at, results);
        }
        if (distMin.y < results[results.length - 1].distance) {
            processCell(cellX, cellY - 1, at, results);
        }
        if (distMax.x < results[results.length - 1].distance) {
            processCell(cellX + 1, cellY, at, results);
        }
        if (distMax.y < results[results.length - 1].distance) {
            processCell(cellX, cellY + 1, at, results);
        }

        // Test further cells
        if (distMin.x + distMin.y < results[results.length - 1].distance) {
            processCell(cellX - 1, cellY - 1, at, results);
        }
        if (distMax.x + distMax.y < results[results.length - 1].distance) {
            processCell(cellX + 1, cellY + 1, at, results);
        }
        if (distMin.x + distMax.y < results[results.length - 1].distance) {
            processCell(cellX - 1, cellY + 1, at, results);
        }
        if (distMax.x + distMin.y < results[results.length - 1].distance) {
            processCell(cellX + 1, cellY - 1, at, results);
        }

        for (VoronoiResult result : results) {
            result.delta.scale(INVERSE_DENSITY_ADJUSTMENT);
            result.distance *= INVERSE_DENSITY_ADJUSTMENT * INVERSE_DENSITY_ADJUSTMENT;
        }

        return results;
    }

    private long incrementSeed(long last) {
        return (1402024253L * last + 586950981L) & 0xFFFFFFFFL;
    }

    private void processCell(int cellX, int cellY, Vector2f at, VoronoiResult[] results) {
        long seed = (702395077 * cellX + 915488749 * cellY);
        // Number of features
        int count = poissonCount[(int) (seed >> 24)];
        seed = incrementSeed(seed);

        for (int point = 0; point < count; point++) {
            long id = seed;
            seed = incrementSeed(seed);

            float x = (seed + 0.5f) / 4294967296.0f;
            seed = incrementSeed(seed);
            float y = (seed + 0.5f) / 4294967296.0f;
            seed = incrementSeed(seed);
            Vector2f innerPos = new Vector2f(x, y);
            Vector2f delta = new Vector2f(cellX + innerPos.x - at.x, cellY + innerPos.y - at.y);

            float dist = standardDistanceFunction(delta);

            if (dist < results[results.length - 1].distance) {
                int index = results.length - 1;
                while (index > 0 && dist < results[index - 1].distance) {
                    index--;
                }

                for (int i = results.length - 1; i > index; i--) {
                    results[i] = results[i - 1];
                }
                results[index].distance = dist;
                results[index].delta = new Vector2f(delta);
                results[index].id = (int) id;
            }
        }
    }

    public static class VoronoiResult {
        public float distance;
        public Vector2f delta;
        public int id;
    }
}
