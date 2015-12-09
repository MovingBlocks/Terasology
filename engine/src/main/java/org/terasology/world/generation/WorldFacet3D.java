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

import org.terasology.math.Region3i;

/**
 */
public interface WorldFacet3D extends WorldFacet {

    /**
     * @return The region of the world covered by this facet
     */
    Region3i getWorldRegion();

    /**
     * @return The region covered by this facet, relative to the target region
     */
    Region3i getRelativeRegion();
}
