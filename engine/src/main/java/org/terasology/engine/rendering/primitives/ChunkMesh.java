// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.primitives;

import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.gestalt.module.sandbox.API;

public interface ChunkMesh {

    void updateMaterial(Material chunkMaterial, Vector3fc chunkPosition, boolean chunkIsAnimated);

    int triangleCount(RenderPhase phase);

    int getTimeToGenerateBlockVertices();

    int getTimeToGenerateOptimizedBuffers();

    void dispose();

    int render(RenderPhase type);

    /**
     * Possible rendering types.
     */
    @API
    enum RenderType {
        OPAQUE(0),
        TRANSLUCENT(1),
        BILLBOARD(2),
        WATER_AND_ICE(3);

        private final int meshIndex;

        RenderType(int index) {
            meshIndex = index;
        }

        public int getIndex() {
            return meshIndex;
        }
    }

    enum RenderPhase {
        OPAQUE,
        ALPHA_REJECT,
        REFRACTIVE,
        Z_PRE_PASS
    }
}
