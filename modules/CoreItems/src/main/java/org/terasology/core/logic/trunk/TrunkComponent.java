/*
 * Copyright 2019 MovingBlocks
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

package org.terasology.core.logic.trunk;

import org.terasology.audio.StaticSound;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.Side;
import org.terasology.world.block.family.BlockFamily;

/**
 */
public class TrunkComponent implements Component {
    public BlockFamily leftBlockFamily;
    public BlockFamily rightBlockFamily;
    public StaticSound openSound;
    public StaticSound closeSound;
    public Prefab trunkRegionPrefab;
}
