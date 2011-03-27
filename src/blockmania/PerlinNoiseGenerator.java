/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package blockmania;

import java.util.Random;

/**
 * 3D perlin noise function as shown in the book "Physically Based Rendering".
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class PerlinNoiseGenerator {

    Random rand;

    static int noisePerm[] = {
        151, 160, 137, 91, 90, 15, 131, 13, 201, 95, 96,
        53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142,
        8, 99, 37, 240, 21, 10, 23,
        190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33,
        88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166,
        77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244,
        102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196,
        135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123,
        5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42,
        223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9,
        129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228,
        251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107,
        49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254,
        138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180,
        151, 160, 137, 91, 90, 15,
        131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23,
        190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33,
        88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166,
        77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244,
        102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196,
        135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123,
        5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42,
        223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9,
        129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228,
        251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107,
        49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254,
        138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
    };

    public PerlinNoiseGenerator(String seed) {
        rand = new Random(seed.hashCode());

        for (int i = 0; i < noisePerm.length; i++) {
            noisePerm[i] = Math.abs(rand.nextInt()) % (noisePerm.length / 2);
        }
    }

    public float noise(float x, float y, float z) {
        // Compute noise cell coordinates and offsets
        int ix = (int) Math.floor(x), iy = (int) Math.floor(y), iz = (int) Math.floor(z);
        float dx = x - ix, dy = y - iy, dz = z - iz;

        // Compute gradient weights
        ix &= (noisePerm.length - 1);
        iy &= (noisePerm.length - 1);
        iz &= (noisePerm.length - 1);

        float w000 = grad(ix, iy, iz, dx, dy, dz);
        float w100 = grad(ix + 1, iy, iz, dx - 1, dy, dz);
        float w010 = grad(ix, iy + 1, iz, dx, dy - 1, dz);
        float w110 = grad(ix + 1, iy + 1, iz, dx - 1, dy - 1, dz);
        float w001 = grad(ix, iy, iz + 1, dx, dy, dz - 1);
        float w101 = grad(ix + 1, iy, iz + 1, dx - 1, dy, dz - 1);
        float w011 = grad(ix, iy + 1, iz + 1, dx, dy - 1, dz - 1);
        float w111 = grad(ix + 1, iy + 1, iz + 1, dx - 1, dy - 1, dz - 1);

        // Compute trilinear interpolation of weights
        float wx = noiseWeight(dx), wy = noiseWeight(dy), wz = noiseWeight(dz);
        float x00 = lerp(wx, w000, w100);
        float x10 = lerp(wx, w010, w110);
        float x01 = lerp(wx, w001, w101);
        float x11 = lerp(wx, w011, w111);
        float y0 = lerp(wy, x00, x10);
        float y1 = lerp(wy, x01, x11);
        return lerp(wz, y0, y1);
    }

    float lerp(float t, float v1, float v2) {
        return (1.f - t) * v1 + t * v2;
    }

    float grad(int x, int y, int z, float dx, float dy, float dz) {
        int h = noisePerm[noisePerm[noisePerm[x] + y] + z];
        h &= 15;
        float u = h < 8 || h == 12 || h == 13 ? dx : dy;
        float v = h < 4 || h == 12 || h == 13 ? dy : dz;

        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    float noiseWeight(float t) {
        float t3 = t * t * t;
        float t4 = t3 * t;
        return 6.f * t4 * t - 15.f * t4 + 10.f * t3;
    }
}
