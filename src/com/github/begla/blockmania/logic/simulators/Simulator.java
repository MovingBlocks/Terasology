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
package com.github.begla.blockmania.logic.simulators;

import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.logic.world.BlockObserver;
import com.github.begla.blockmania.logic.world.WorldProvider;
import com.github.begla.blockmania.model.structures.BlockPosition;

import java.util.HashSet;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Simulator implements BlockObserver {

    private boolean _running = false;

    protected final long _updateInterval;
    protected long _lastUpdate = Blockmania.getInstance().getTime();

    protected final WorldProvider _parent;
    protected final HashSet<BlockPosition> _activeBlocks = new HashSet<BlockPosition>(256);

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

        if ((currentTime > _lastUpdate + _updateInterval || force) && !_running) {

            _running = true;

            // Create a new thread and start processing
            Runnable r = new Runnable() {
                public void run() {
                    executeSimulation();
                    _running = false;
                }
            };

            Blockmania.getInstance().getThreadPool().execute(r);

            _lastUpdate = currentTime;
            return true;
        }

        return false;
    }

    public void clear() {
        _activeBlocks.clear();
    }

    protected abstract boolean executeSimulation();
}