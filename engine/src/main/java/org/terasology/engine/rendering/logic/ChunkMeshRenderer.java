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
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.rendering.world.RenderableWorld;
import org.terasology.engine.world.chunks.RenderableChunk;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles entities with ChunkMeshComponents, and passes them to the rendering engine.
 *
 * Also handles disposing of the mesh data, assuming the mesh is never changed after being added.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ChunkMeshRenderer extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(ChunkMeshRenderer.class);

    @In
    private RenderableWorld renderableWorld;

    private Map<EntityRef, EntityBasedRenderableChunk> pseudoChunks = new HashMap<>();
    private ReferenceQueue<ChunkMesh> disposalQueue = new ReferenceQueue<>();
    private Set<DisposableChunkMesh> disposalSet = new HashSet<>();

    @Override
    public void initialise() {
        renderableWorld.setChunkMeshRenderer(this);
    }

    @ReceiveEvent(components = {ChunkMeshComponent.class, LocationComponent.class})
    public void onNewMesh(OnActivatedComponent event, EntityRef entity, ChunkMeshComponent mesh) {
        pseudoChunks.put(entity, new EntityBasedRenderableChunk(entity));
        disposalSet.add(new DisposableChunkMesh(mesh.mesh, disposalQueue));
    }

    @ReceiveEvent(components = {MeshComponent.class, LocationComponent.class})
    public void onDestroyMesh(BeforeDeactivateComponent event, EntityRef entity) {
        pseudoChunks.remove(entity);
    }

    public Collection<? extends RenderableChunk> getRenderableChunks() {
        return pseudoChunks.values();
    }

    @Override
    public void update(float delta) {
        Reference<? extends ChunkMesh> reference = null;
        while ((reference = disposalQueue.poll()) != null) {
            DisposableChunkMesh hook = (DisposableChunkMesh) reference;
            hook.cleanup();
        }
    }

    @Override
    public void shutdown() {
        for (DisposableChunkMesh reference : disposalSet) {
            reference.cleanup();
        }
    }

    private class DisposableChunkMesh extends PhantomReference<ChunkMesh> {
        private final ChunkMesh.DisposableHook hook;

        public DisposableChunkMesh(ChunkMesh referent, ReferenceQueue<? super ChunkMesh> q) {
            super(referent, q);
            hook = referent.disposalHook();
        }

        public void cleanup() {
            hook.dispose();
        }
    }

}
