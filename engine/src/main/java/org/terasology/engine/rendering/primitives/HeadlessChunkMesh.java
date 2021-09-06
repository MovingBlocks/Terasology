// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.primitives;

import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.material.Material;

public class HeadlessChunkMesh implements ChunkMesh {
    private VertexElements[] vertexElements = new VertexElements[ChunkMesh.RenderType.values().length];

    public HeadlessChunkMesh() {
        for (ChunkMesh.RenderType type : ChunkMesh.RenderType.values()) {
            vertexElements[type.ordinal()] = new VertexElements();
        }
    }

    @Override
    public VertexElements getVertexElements(RenderType renderType) {
        return vertexElements[renderType.ordinal()];
    }

    @Override
    public boolean hasVertexElements() {
        return vertexElements != null;
    }

    @Override
    public boolean updateMesh() {
        return true;
    }

    @Override
    public void discardData() {
    }

    @Override
    public void updateMaterial(Material chunkMaterial, Vector3fc chunkPosition, boolean chunkIsAnimated) {

    }

    @Override
    public int triangleCount(RenderPhase phase) {
        return 0;
    }

    @Override
    public int getTimeToGenerateBlockVertices() {
        return 0;
    }

    @Override
    public int getTimeToGenerateOptimizedBuffers() {
        return 0;
    }

    @Override
    public void dispose() {

    }

    @Override
    public int render(RenderPhase type) {
        return 0;
    }

    @Override
    public DisposableHook disposalHook() {
        return () -> {
        };
    }
}
