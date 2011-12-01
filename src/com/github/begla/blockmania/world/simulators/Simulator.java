/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.world.simulators;

import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.world.chunk.Chunk;
import com.github.begla.blockmania.world.main.WorldProvider;

import java.util.ArrayList;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Simulator {

    protected WorldProvider _parent;
    protected ArrayList<BlockPosition> _activeBlocks = new ArrayList<BlockPosition>(16);
    protected ArrayList<Chunk> _activeChunks = new ArrayList<Chunk>(16);

    public Simulator(WorldProvider parent) {
        _parent = parent;
    }

    public void addActiveBlock(BlockPosition bp) {
        _activeBlocks.add(bp);
    }

    public void addActiveChunk(Chunk c) {
        _activeChunks.add(c);
    }

    public abstract void simulate();
}