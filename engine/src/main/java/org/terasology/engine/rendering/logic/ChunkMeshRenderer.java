// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.world.RenderableWorld;
import org.terasology.engine.world.chunks.RenderableChunk;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles entities with ChunkMeshComponents, and passes them to the rendering engine.
 *
 * Also handles disposing of the mesh data, assuming the mesh is never changed after being added.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ChunkMeshRenderer extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ChunkMeshRenderer.class);

    @In
    private RenderableWorld renderableWorld;

    private Map<EntityRef, EntityBasedRenderableChunk> pseudoChunks = new HashMap<>();

    @Override
    public void initialise() {
        renderableWorld.setChunkMeshRenderer(this);
    }

    @ReceiveEvent(components = {ChunkMeshComponent.class, LocationComponent.class})
    public void onNewMesh(OnActivatedComponent event, EntityRef entity, ChunkMeshComponent mesh) {
        pseudoChunks.put(entity, new EntityBasedRenderableChunk(entity));
    }

    @ReceiveEvent(components = {MeshComponent.class})
    public void onDestroyMesh(BeforeDeactivateComponent event, EntityRef entity) {
        EntityBasedRenderableChunk renderableChunk = pseudoChunks.remove(entity);
        if (renderableChunk != null) {
            renderableChunk.disposeMesh();
        }
    }

    public Collection<? extends RenderableChunk> getRenderableChunks() {
        return pseudoChunks.values();
    }

    @Override
    public void shutdown() {
        for (EntityBasedRenderableChunk reference : pseudoChunks.values()) {
            reference.disposeMesh();
        }
    }
}
