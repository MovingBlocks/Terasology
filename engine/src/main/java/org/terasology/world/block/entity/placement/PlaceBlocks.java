/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.block.entity.placement;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;

import java.util.Collections;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PlaceBlocks extends AbstractConsumableEvent {
    private Map<Vector3i, Block> blocks;
    private EntityRef instigator;

    public PlaceBlocks(Vector3i location, Block block) {
        this(location, block, EntityRef.NULL);
    }

    public PlaceBlocks(Map<Vector3i, Block> blocks) {
        this(blocks, EntityRef.NULL);
    }

    public PlaceBlocks(Vector3i location, Block block, EntityRef instigator) {
        blocks = Collections.singletonMap(location, block);
        this.instigator = instigator;
    }

    public PlaceBlocks(Map<Vector3i, Block> blocks, EntityRef instigator) {
        this.blocks = blocks;
        this.instigator = instigator;
    }

    public Map<Vector3i, Block> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
