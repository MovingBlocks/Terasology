/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.utilities;

import gnu.trove.list.array.TDoubleArrayList;
import org.lwjgl.util.vector.Vector3f;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class VoronoiNoise {

    int _seed;

    public VoronoiNoise(int seed) {
        _seed = seed;
    }

    public double noise(double x, double y, double z, double _frequency) {
        TDoubleArrayList candidates = new TDoubleArrayList();

        x *= _frequency;
        y *= _frequency;
        z *= _frequency;

        for (int zCubePos = (int) z - 1; zCubePos <= (int) z + 1; zCubePos++) {
            for (int yCubePos = (int) y - 1; yCubePos <= (int) y + 1; yCubePos++) {
                for (int xCubePos = (int) x - 1; xCubePos <= (int) x + 1; xCubePos++) {
                    double candidateX = (xCubePos +
                            (FastRandom.randomNoise(xCubePos, yCubePos, zCubePos, _seed) + 1.0) / 2.0);
                    double candidateY = (yCubePos +
                            (FastRandom.randomNoise(xCubePos, yCubePos, zCubePos, _seed + 1) + 1.0) / 2.0);
                    double candidateZ = (zCubePos +
                            (FastRandom.randomNoise(xCubePos, yCubePos, zCubePos, _seed + 2) + 1) / 2.0);

                    double distX = candidateX - x;
                    double distY = candidateY - y;
                    double distZ = candidateZ - z;

                    double candidateDistance = distX * distX + distY * distY + distZ * distZ;
                    candidates.add(candidateDistance);
                }
            }
        }

        candidates.sort();
        double dist = -1 * candidates.get(0) + candidates.get(1);

        return (dist + 1) / 2;
    }

}
