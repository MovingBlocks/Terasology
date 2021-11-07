// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.primitives;

import org.joml.Vector3f;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockPart;
import org.terasology.engine.world.block.shapes.BlockMeshPart;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.Color;

public abstract class BlockMeshShapeGenerator implements BlockMeshGenerator {
    protected Mesh mesh = null;

    public abstract Block getBlock();

    public abstract ResourceUrn baseUrn();

    @Override
    public Mesh getStandaloneMesh() {
        if (mesh == null || mesh.isDisposed()) {
            Block block = getBlock();
            StandardMeshData meshData = new StandardMeshData();
            int nextIndex = 0;
            Vector3f light0 = new Vector3f(1,1,1);
            for (BlockPart dir : BlockPart.allParts()) {
                BlockMeshPart part = block.getPrimaryAppearance().getPart(dir);
                if (part != null) {
                    for (int i = 0; i < part.size(); i++) {
                        meshData.position.put(part.getVertex(i));
                        meshData.color0.put(Color.white);
                        meshData.normal.put(part.getNormal(i));
                        meshData.uv0.put(part.getTexCoord(i));
                        meshData.light0.put(light0);
                    }
                    for (int i = 0; i < part.indicesSize(); ++i) {
                        meshData.indices.put(nextIndex + part.getIndex(i));
                    }
                    if (block.isDoubleSided()) {
                        for (int i = 0; i < part.indicesSize(); i += 3) {
                            meshData.indices.put(nextIndex + part.getIndex(i + 1));
                            meshData.indices.put(nextIndex + part.getIndex(i));
                            meshData.indices.put(nextIndex + part.getIndex(i + 2));
                        }
                    }
                    nextIndex += part.size();
                }
            }
            mesh = Assets.generateAsset(
                    new ResourceUrn(baseUrn(), block.getURI().toString()), meshData,
                    Mesh.class);
        }
        return mesh;
    }
}
