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
package org.terasology.world.block;

import org.terasology.math.geom.Vector4f;
import org.terasology.world.generation.Region;

/**
 * Used to determine a multiplicative color for certain blocks based on the block's world conditions.
 */
@FunctionalInterface
public interface BlockColorSource {

    default Vector4f calcColor(Region worldData) {
        return calcColor(worldData, 0, 0, 0);
    };

   default Vector4f calcColor(Region worldData, int x, int z) {
       return calcColor(worldData, x, 0, z);
   };

    Vector4f calcColor(Region worldData, int x, int y, int z);

}
