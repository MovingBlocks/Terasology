// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;


import org.terasology.gestalt.module.sandbox.API;

/**
 * Chunks are a box-shaped logical grouping of Terasology's blocks, for performance reasons.
 * <p>
 * For example the renderer renders a single mesh for all opaque blocks in a chunk rather than rendering each block as a
 * separate mesh.
 * <p>
 * For details on dimensions and other chunks characteristics see {@link ChunkConstants}.
 */
@API
public interface Chunk extends ManagedChunk, RenderableChunk {
}
