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
package org.terasology.world.generation;

import org.terasology.math.Rect2i;
import org.terasology.world.generation.providers.BatchSurfaceHeightProvider;
import org.terasology.world.generation.providers.SurfaceHeightProvider;

/**
 * @author Immortius
 */
public class BatchSurfaceHeigherProviderImpl implements BatchSurfaceHeightProvider {

    @Requires
    private SurfaceHeightProvider surfaceHeightProvider;

    @Override
    public float[] getSurfaceHeights(Rect2i area) {
        float[] data = new float[area.size().x * area.size().y];
        int sizeX = area.size().x;
        int sizeY = area.size().y;
        for (int y = 0; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {
                data[x + sizeX * y] = surfaceHeightProvider.getHeightAt(x + area.minX(), y + area.minY());
            }
        }
        return data;
    }

    @Override
    public void setSeed(long seed) {

    }
}
