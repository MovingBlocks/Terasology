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


import org.terasology.module.sandbox.API;

/**
 * Chunks are the smallest logical units of the worlds.
 *
 * Chunks are the default unit passed to world generator during world generation.
 * For details on dimensions and other parameters of chunk, check {@link ChunkConstants}.
 */
@API
public interface Chunk extends ManagedChunk, RenderableChunk {
}
