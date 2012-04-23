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
package org.terasology.logic.simulators;

import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.Terasology;
import org.terasology.game.Timer;
import org.terasology.logic.world.IBlockObserver;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.model.structures.BlockPosition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base class for all simulators.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Simulator implements IBlockObserver {

    private final String _name;
    private boolean _running = false;

    protected final long _updateInterval;
    protected long _lastUpdate;

    protected final IWorldProvider _parent;
    protected final Set<BlockPosition> _activeBlocks = new HashSet<BlockPosition>();
    protected Timer _timer;

    protected final ReentrantLock _lock = new ReentrantLock();

    public Simulator(String name, IWorldProvider parent, long updateInterval) {
        _updateInterval = updateInterval;
        _parent = parent;
        _name = name;
        _timer = CoreRegistry.get(Timer.class);
        _lastUpdate = _timer.getTimeInMs();
    }

    public Simulator(String name, IWorldProvider parent) {
        this(name, parent, 1000);
    }

    public BlockPosition tryRemoveFirstBlock() {
        try {
            if (_lock.tryLock()) {
                if (_activeBlocks.size() > 0) {
                    BlockPosition bp = _activeBlocks.iterator().next();
                    _activeBlocks.remove(bp);
                    return bp;
                }
            }
        } finally {
            _lock.unlock();
        }

        return null;
    }

    public ArrayList<BlockPosition> tryCopyActiveBlocks() {
        try {
            if (_lock.tryLock()) {
                return new ArrayList<BlockPosition>(_activeBlocks);
            }
        } finally {
            _lock.unlock();
        }

        return null;
    }

    public void addActiveBlock(BlockPosition bp) {
        try {
            _lock.lock();
            _activeBlocks.add(bp);
        } finally {
            _lock.unlock();
        }
    }

    public void clear() {
        try {
            _lock.lock();
            _activeBlocks.clear();
        } finally {
            _lock.unlock();
        }
    }

    public void simulateAll() {
        if (_running)
            return;

        _running = true;

        // Create a new thread and start processing
        Runnable r = new Runnable() {
            public void run() {
                while (executeSimulation()) ;
                _running = false;
            }
        };

        CoreRegistry.get(GameEngine.class).submitTask(_name + "Complete", r);
    }

    public boolean simulate(boolean force) {
        if (_running)
            return false;

        long currentTime = _timer.getTimeInMs();

        if ((currentTime > _lastUpdate + _updateInterval || force)) {

            _running = true;

            // Create a new thread and start processing
            Runnable r = new Runnable() {
                public void run() {
                    executeSimulation();
                    _running = false;
                }
            };

            CoreRegistry.get(GameEngine.class).submitTask(_name, r);

            _lastUpdate = currentTime;
            return true;
        }

        return false;
    }

    protected abstract boolean executeSimulation();
}