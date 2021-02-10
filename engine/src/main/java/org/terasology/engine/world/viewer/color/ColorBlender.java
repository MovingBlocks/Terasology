/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.viewer.color;


/**
 * Maps or blends color from a source into a target color space.
 */
public interface ColorBlender {

    /**
     * Converts a color into target space.
     * @param src the source color in an implementation-dependent color space
     * @return the color in the target color space
     */
    int get(int src);

    /**
     * Adds a color from the source space on top of a color in dst color space
     * @param src the source color in an implementation-dependent color space
     * @param dst the dest. color
     * @return the color (src + dst) in the target color space
     */
    int add(int src, int dst);

    /**
     * Blends a color using source alpha from the source space on top of a color in dst color space
     * @param src the source color in an implementation-dependent color space
     * @param dst the dest. color
     * @return the color (a * s + (1 - a) * dst) in the target color space
     */
    int blend(int src, int dst);
}
