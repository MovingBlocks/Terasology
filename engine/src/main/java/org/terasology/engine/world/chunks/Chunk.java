// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;


import org.terasology.module.sandbox.API;

/**
 * Chunks are a box-shaped logical grouping of Terasology's blocks, for performance reasons. 
 *
 * For example the renderer renders a single mesh for all opaque blocks in a chunk rather
 * than rendering each block as a separate mesh.
 *
 * For details on dimensions and other chunks characteristics see {@link Chunks}.
 */
@API
public interface Chunk extends ManagedChunk, RenderableChunk {
}
