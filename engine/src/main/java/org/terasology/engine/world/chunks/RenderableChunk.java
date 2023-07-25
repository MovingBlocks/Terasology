// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;

import org.joml.Vector3f;
import org.terasology.engine.rust.resource.ChunkGeometry;
import org.terasology.joml.geom.AABBfc;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.engine.rendering.primitives.ChunkMesh;

/**
 * Anything that acts like a chunk for rendering purposes
 */
@API
public interface RenderableChunk {

    Vector3f getRenderPosition();

    AABBfc getAABB();

    void setMesh(ChunkGeometry newMesh);

    void setAnimated(boolean animated);

    boolean isAnimated();

    boolean hasMesh();

    ChunkGeometry getMesh();

    void disposeMesh();

    boolean isReady();

}
