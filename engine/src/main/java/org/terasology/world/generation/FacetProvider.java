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

/**
 */
public interface FacetProvider {

    /**
     * @param seed the seed value (typically used for random number generators)
     */
    default void setSeed(long seed) {
        // don't do anything
    }

    /**
     * This is always called after {@link #setSeed(long)}.
     */
    default void initialize() {
        // don't do anything
    }

    void process(GeneratingRegion region);
}
