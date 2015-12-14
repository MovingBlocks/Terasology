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
package org.terasology.logic.debug;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.WorldComponent;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;

import java.util.Set;

/**
 */
@RegisterSystem
public class ChunkEventErrorLogger extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ChunkEventErrorLogger.class);

    private Set<Vector3i> loadedChunks = Sets.newHashSet();

    @ReceiveEvent(components = {WorldComponent.class})
    public void onNewChunk(OnChunkLoaded chunkAvailable, EntityRef worldEntity) {
        if (!loadedChunks.add(chunkAvailable.getChunkPos())) {
            logger.error("Multiple loads of chunk {}", chunkAvailable.getChunkPos());
        }
    }

    @ReceiveEvent(components = {WorldComponent.class})
    public void onRemoveChunk(BeforeChunkUnload chunkUnload, EntityRef worldEntity) {
        if (!loadedChunks.remove(chunkUnload.getChunkPos())) {
            logger.error("Unload event for not loaded chunk {}", chunkUnload.getChunkPos());
        }
    }
}
