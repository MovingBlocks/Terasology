// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.world;

import org.joml.Vector3i;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;

/**
 */
@RegisterSystem(RegisterMode.CLIENT)
public class WorldRendererSystem extends BaseComponentSystem {

    @In
    private WorldRenderer worldRenderer;

    @ReceiveEvent(components = WorldComponent.class)
    public void onChunkLoaded(OnChunkLoaded chunkLoaded, EntityRef entity) {
        worldRenderer.onChunkLoaded(new Vector3i(chunkLoaded.getChunkPos()));
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void onChunkUnloaded(BeforeChunkUnload chunkUnloaded, EntityRef entity) {
        worldRenderer.onChunkUnloaded(new Vector3i(chunkUnloaded.getChunkPos()));
    }



}
