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

import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

/**
 * Some white noise
 *
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
public class WhiteNoise implements Noise3D {

    private final Random rand;

    /**
     * Initialize a new generator with a given seed value.
     *
     * @param seed The seed value
     */
    public WhiteNoise(int seed) {
        rand = new FastRandom(seed);
    }

    /**
     * Generates noise in the range [-1..1] 
     */
    @Override
    public double noise(double x, double y, double z) {
        return rand.nextDouble(-1.0f, 1.0f);
    }

}
