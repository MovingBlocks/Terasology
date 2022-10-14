// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.logic;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.world.chunks.RenderableChunk;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;

/**
 * A wrapper around an entity with both a ChunkMeshComponent and a LocationComponent,
 * allowing it to be rendered like a chunk.
 */
public class EntityBasedRenderableChunk implements RenderableChunk {
    private EntityRef entity;

    public EntityBasedRenderableChunk(EntityRef entity) {
        this.entity = entity;
    }

    @Override
    public Vector3f getRenderPosition() {
        return entity.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
    }

    @Override
    public AABBfc getAABB() {
        return entity.getComponent(ChunkMeshComponent.class).aabb.translate(getRenderPosition(), new AABBf());
    }

    @Override
    public void setMesh(ChunkMesh newMesh) {
        entity.updateComponent(ChunkMeshComponent.class, c -> {
            c.setMesh(newMesh);
            return c;
        });
    }

    @Override
    public void setAnimated(boolean animated) {
        ChunkMeshComponent component = entity.getComponent(ChunkMeshComponent.class);
        component.animated = animated;
        entity.saveComponent(component);
    }

    @Override
    public boolean isAnimated() {
        return entity.getComponent(ChunkMeshComponent.class).animated;
    }

    @Override
    public boolean hasMesh() {
        ChunkMeshComponent mesh = entity.getComponent(ChunkMeshComponent.class);
        LocationComponent location = entity.getComponent(LocationComponent.class);
        return mesh != null && location != null && mesh.mesh != null;
    }

    @Override
    public ChunkMesh getMesh() {
        return entity.getComponent(ChunkMeshComponent.class).mesh;
    }

    @Override
    public void disposeMesh() {
        entity.updateComponent(ChunkMeshComponent.class, c -> {
            c.setMesh(null);
            return c;
        });
    }

    @Override
    public boolean isReady() {
        return hasMesh();
    }
}
