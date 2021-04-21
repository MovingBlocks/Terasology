// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.logic;

import org.lwjgl.opengl.GL15;
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
    @In
    private RenderableWorld renderableWorld;

    private Map<EntityRef, EntityBasedRenderableChunk> pseudoChunks = new HashMap<>();
    private ReferenceQueue<ChunkMesh> disposalQueue = new ReferenceQueue<>();
    private Set<BuffersReference> disposalSet = new HashSet<>();

    @Override
    public void initialise() {
        renderableWorld.setChunkMeshRenderer(this);
    }

    @ReceiveEvent(components = {ChunkMeshComponent.class, LocationComponent.class})
    public void onNewMesh(OnActivatedComponent event, EntityRef entity, ChunkMeshComponent mesh) {
        pseudoChunks.put(entity, new EntityBasedRenderableChunk(entity));
        disposalSet.add(new BuffersReference(mesh.mesh, disposalQueue));
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
        BuffersReference reference = (BuffersReference) disposalQueue.poll();
        while (reference != null) {
            GL15.glDeleteBuffers(reference.bufferIds);
            disposalSet.remove(reference);
            reference = (BuffersReference) disposalQueue.poll();
        }
    }

    @Override
    public void shutdown() {
        for (BuffersReference reference : disposalSet) {
            GL15.glDeleteBuffers(reference.bufferIds);
        }
    }

    private static class BuffersReference extends PhantomReference<ChunkMesh> {
        int[] bufferIds;

        BuffersReference(ChunkMesh mesh, ReferenceQueue<ChunkMesh> queue) {
            super(mesh, queue);
            bufferIds = new int[mesh.vertexBuffers.length + mesh.idxBuffers.length];
            System.arraycopy(mesh.vertexBuffers, 0, bufferIds, 0, mesh.vertexBuffers.length);
            System.arraycopy(mesh.idxBuffers, 0, bufferIds, mesh.vertexBuffers.length, mesh.idxBuffers.length);
        }
    }
}
