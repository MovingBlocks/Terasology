// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

@RegisterSystem(RegisterMode.CLIENT)
public class WorldRendererSystem extends BaseComponentSystem {

    @In
    private WorldRenderer worldRenderer;

    @ReceiveEvent(components = WorldComponent.class)
    public void onChunkLoaded(OnChunkLoaded chunkLoaded, EntityRef entity) {
        worldRenderer.onChunkLoaded(chunkLoaded.getChunkPos());
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void onChunkUnloaded(BeforeChunkUnload chunkUnloaded, EntityRef entity) {
        worldRenderer.onChunkUnloaded(chunkUnloaded.getChunkPos());
    }



}
