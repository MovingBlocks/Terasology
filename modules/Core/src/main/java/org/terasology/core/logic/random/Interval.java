/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.core.logic.random;

import org.terasology.reflection.MappedContainer;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

/**
 * A representation of a uniformly-distributed integer range, for use as a datatype by {@code Component}s
 *
 * For example, suppose that {@code ExampleComponent} is a {@link org.terasology.entitySystem.Component} whose {@code
 * timeToGrow} field is an {@code Interval}.  Then the following prefab snippet says that, whenever the {@code timeToGrow}
 * is {@link #sample}d, a random number will be returned that is uniformly distributed between 1000 and 6000.
 *
 * {@code
 *   "ExampleComponent": {
 *     "timeToGrow": {
 *       "maxRandom": 1000
 *       "fixed": 5000
 *     }
 *   }
 * }
 */
@MappedContainer
public class Interval {
    /**
     * Minimum bounds of variation.
     *
     * The default value of 0 is probably correct for most applications, since {@code fixed} and {@code maxRandom} are
     * sufficient to describe the interval, but this field is still provided for those situations where a different value is
     * more convenient (e.g., 1000 +/- 37 is clearer than [963, 1037]).
     */
    public long minRandom = 0;

    /**
     * Maximum bounds of variation.
     *
     * It probably doesn't make sense to have this be less than {@code minRandom}, but this constraint is not enforced.
     */
    public long maxRandom = 5000;

    /** The base number before random variation is applied. */
    public long fixed = 10000;

    /**
     * Return a random integer uniformly distributed between {@code fixed + minRandom} and {@code fixed + maxRandom}.
     */
    public long sample() {
        long total = fixed;
        Random random = new FastRandom();
        total += random.nextFloat(minRandom, maxRandom);
        return total;
    }
}
