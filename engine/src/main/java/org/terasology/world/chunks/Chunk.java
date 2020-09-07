/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.chunks;


import org.terasology.gestalt.module.sandbox.API;

/**
 * Chunks are a box-shaped logical grouping of Terasology's blocks, for performance reasons. 
 *
 * For example the renderer renders a single mesh for all opaque blocks in a chunk rather
 * than rendering each block as a separate mesh.
 *
 * For details on dimensions and other chunks characteristics see {@link ChunkConstants}.
 */
@API
public interface Chunk extends ManagedChunk, RenderableChunk {
}
