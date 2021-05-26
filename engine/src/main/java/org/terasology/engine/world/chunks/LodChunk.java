// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;
import org.terasology.engine.rendering.primitives.ChunkMesh;

/**
 * A static, far away chunk that has only the data needed for rendering.
 */
public class LodChunk implements RenderableChunk {
    public final int scale;
    public int hiddenness; //The number of LOD chunks of the next level of fineness covering this one.
    public Chunk realVersion; //The real chunk hiding this one.
    private Vector3ic position;
    private ChunkMesh mesh;

    public LodChunk(Vector3ic pos, ChunkMesh mesh, int scale) {
        position = pos;
        this.mesh = mesh;
        this.scale = scale;
    }

    public Vector3i getPosition(Vector3i dest) {
        return dest.set(position);
    }


    @Override
    public Vector3f getRenderPosition() {
        return new Vector3f(position).mul(Chunks.SIZE_X, Chunks.SIZE_Y, Chunks.SIZE_Z);
    }

    @Override
    public AABBfc getAABB() {
        Vector3f min = getRenderPosition();
        return new AABBf(min, new Vector3f(Chunks.CHUNK_SIZE).mul(1 << scale).add(min));
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public void setAnimated(boolean animated) {
    }

    @Override
    public boolean hasMesh() {
        return true;
    }

    @Override
    public ChunkMesh getMesh() {
        return mesh;
    }

    @Override
    public void setMesh(ChunkMesh newMesh) {
        mesh = newMesh;
    }

    @Override
    public void disposeMesh() {
        if (mesh != null) {
            mesh.dispose();
            mesh = null;
        }
    }

    @Override
    public boolean isReady() {
        return mesh != null && hiddenness < 8 && (realVersion == null || !realVersion.isReady() || !realVersion.hasMesh());
    }
}
