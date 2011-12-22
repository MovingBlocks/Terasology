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
import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.world.interfaces.BlockObserver;
import com.github.begla.blockmania.world.interfaces.WorldProvider;

import java.util.HashSet;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Simulator implements BlockObserver {

    protected final long _updateInterval;
    protected long _lastUpdate = Blockmania.getInstance().getTime();

    protected final WorldProvider _parent;
    protected final HashSet<BlockPosition> _activeBlocks = new HashSet<BlockPosition>(16);

    public Simulator(WorldProvider parent, long updateInterval) {
        _updateInterval = updateInterval;
        _parent = parent;
    }

    public Simulator(WorldProvider parent) {
        this(parent, 1000);
    }

    public void addActiveBlock(BlockPosition bp) {
        _activeBlocks.add(bp);
    }

    public boolean simulate(boolean force) {
        long currentTime = Blockmania.getInstance().getTime();

        boolean simulated = false;
        if (currentTime > _lastUpdate + _updateInterval || force) {
            if (executeSimulation()) {
                simulated = true;
            }
            _lastUpdate = currentTime;
        }

        return simulated;
    }

    public void clear() {
        _activeBlocks.clear();
    }

    protected abstract boolean executeSimulation();
}