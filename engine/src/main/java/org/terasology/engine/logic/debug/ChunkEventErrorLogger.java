// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.debug;

import com.google.common.collect.Sets;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.util.Set;

@RegisterSystem
public class ChunkEventErrorLogger extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ChunkEventErrorLogger.class);

    private Set<Vector3ic> loadedChunks = Sets.newHashSet();

    @ReceiveEvent(components = WorldComponent.class)
    public void onNewChunk(OnChunkLoaded chunkAvailable, EntityRef worldEntity) {
        if (!loadedChunks.add(chunkAvailable.getChunkPos())) {
            logger.error("Multiple loads of chunk {}", chunkAvailable.getChunkPos()); //NOPMD
        }
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void onRemoveChunk(BeforeChunkUnload chunkUnload, EntityRef worldEntity) {
        if (!loadedChunks.remove(chunkUnload.getChunkPos())) {
            logger.error("Unload event for not loaded chunk {}", chunkUnload.getChunkPos()); //NOPMD
        }
    }
}
