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
package org.terasology.entitySystem.sectors;

import org.joml.Vector3i;
import org.terasology.entitySystem.Component;
import org.terasology.module.sandbox.API;
import org.terasology.world.chunks.Chunk;

import java.util.HashSet;
import java.util.Set;

/**
 * When this component is added to a sector-scope entity, the {@link SectorSimulationComponent} will count any chunks
 * listed in {@link #chunks} as watched chunks.
 *
 * {@link SectorUtil} contains helper methods for creating this component, and for modifying it using {@link Chunk},
 * rather than positions, if desired.
 */
@API
public class SectorRegionComponent implements Component {

    /**
     * The set of positions of chunks for this entity to watch.
     */
    public Set<Vector3i> chunks = new HashSet<>();

}
