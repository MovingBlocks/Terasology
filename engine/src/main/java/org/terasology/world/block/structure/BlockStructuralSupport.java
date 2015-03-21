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
package org.terasology.world.block.structure;

import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;

import java.util.Map;

public interface BlockStructuralSupport {
    /**
     * Returns priority of this check. All checks with higher priority (lower number) will be invoked before checks
     * with lower priority (higher number).
     *
     * @return
     */
    int getPriority();

    /**
     * Checks if the block at specified location is sufficiently supported according to this class.
     *
     * @param location    Location of the block to check.
     * @param sideChanged Side that has triggered this check.
     * @return If the block should be immediately removed.
     */
    boolean shouldBeRemovedDueToChange(Vector3i location, Side sideChanged);

    /**
     * Checks if the block at location is sufficiently supported.
     *
     * @param location
     * @param blockOverrides
     * @return
     */
    boolean isSufficientlySupported(Vector3i location, Map<Vector3i, Block> blockOverrides);
}
