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
package org.terasology.utilities.procedural;

import com.google.common.math.IntMath;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;

/**
 * @deprecated Use {@link SubSampledNoise} instead
 */
@Deprecated
public class SubSampledNoise2D implements Noise2D {

    private Noise2D source;
    private Vector2f zoom = new Vector2f(1, 1);
    private int sampleRate = 1;

    public SubSampledNoise2D(Noise2D source, Vector2f zoom, int sampleRate) {
        this.source = source;
        this.zoom.set(zoom);
        this.sampleRate = sampleRate;
    }

    @Override
    public float noise(float x, float y) {
        float xMod = TeraMath.modulus(x, sampleRate);
        float yMod = TeraMath.modulus(y, sampleRate);

        float x0 = x - xMod;
        float x1 = x0 + sampleRate;
        float y0 = y - yMod;
        float y1 = y0 + sampleRate;

        float q00 = source.noise(x0 * zoom.x, y0 * zoom.y);
        float q10 = source.noise(x1 * zoom.x, y0 * zoom.y);
        float q01 = source.noise(x0 * zoom.x, y1 * zoom.y);
        float q11 = source.noise(x1 * zoom.x, y1 * zoom.y);

        return TeraMath.biLerp(q00, q10, q01, q11, xMod / sampleRate, yMod / sampleRate);
    }

    public float[] noise(Rect2i region) {
        Rect2i fullRegion = determineRequiredRegion(region);
        float[] keyData = getKeyValues(fullRegion);
        float[] fullData = mapExpand(keyData, fullRegion);
        return getSubset(fullData, fullRegion, region);
    }

    private float[] getSubset(float[] fullData, Rect2i fullRegion, Rect2i subRegion) {
        if (subRegion.size().x != fullRegion.size().x || subRegion.size().y != fullRegion.size().y) {
            float[] result = new float[subRegion.size().x * subRegion.size().y];
            Vector2i offset = new Vector2i(subRegion.minX() - fullRegion.minX(), subRegion.minY() - fullRegion.minY());
            for (int y = 0; y < subRegion.size().y; ++y) {
                System.arraycopy(fullData, offset.getX() + fullRegion.size().x * (y + offset.getY()), result, subRegion.size().x * y, subRegion.size().x);
            }
            return result;
        } else {
            return fullData;
        }
    }

    private float[] mapExpand(float[] keyData, Rect2i fullRegion) {
        float[] fullData = new float[fullRegion.size().x * fullRegion.size().y];
        int samplesX = fullRegion.size().x / sampleRate + 1;
        int samplesY = fullRegion.size().y / sampleRate + 1;
        for (int y = 0; y < samplesY - 1; y++) {
            for (int x = 0; x < samplesX - 1; x++) {
                float q11 = keyData[x + y * samplesX];
                float q21 = keyData[x + 1 + y * samplesX];
                float q12 = keyData[x + (y + 1) * samplesX];
                float q22 = keyData[(x + 1) + (y + 1) * samplesX];
                for (int innerY = 0; innerY < sampleRate; ++innerY) {
                    for (int innerX = 0; innerX < sampleRate; ++innerX) {
                        fullData[x * sampleRate + innerX + fullRegion.size().x * (y * sampleRate + innerY)] =
                                TeraMath.biLerp(q11, q21, q12, q22, (float) innerX / sampleRate, (float) innerY / sampleRate);
                    }
                }
            }
        }
        return fullData;
    }

    private float[] getKeyValues(Rect2i fullRegion) {
        int xDim = fullRegion.size().x / sampleRate + 1;
        int yDim = fullRegion.size().y / sampleRate + 1;
        float[] fullData = new float[xDim * yDim];
        for (int y = 0; y < yDim; y++) {
            for (int x = 0; x < xDim; x++) {
                int actualX = x * sampleRate + fullRegion.minX();
                int actualY = y * sampleRate + fullRegion.minY();
                fullData[x + y * xDim] = source.noise(zoom.x * actualX, zoom.y * actualY);
            }
        }
        return fullData;
    }

    private Rect2i determineRequiredRegion(Rect2i region) {
        int newMinX = region.minX() - IntMath.mod(region.minX(), sampleRate);
        int newMinY = region.minY() - IntMath.mod(region.minY(), sampleRate);
        int newMaxX = region.maxX() + 4 - IntMath.mod(region.maxX(), sampleRate) - 1;
        int newMaxY = region.maxY() + 4 - IntMath.mod(region.maxY(), sampleRate) - 1;
        return Rect2i.createFromMinAndMax(newMinX, newMinY, newMaxX, newMaxY);
    }
}
