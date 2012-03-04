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

import org.terasology.game.Terasology;
import org.terasology.logic.world.IBlockObserver;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.model.structures.BlockPosition;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all simulators.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Simulator implements IBlockObserver {

    private final String _name;
    private boolean _running = false;

    protected final long _updateInterval;
    protected long _lastUpdate = Terasology.getInstance().getTimeInMs();

    protected final IWorldProvider _parent;
    protected final Set<BlockPosition> _activeBlocks = Collections.synchronizedSet(new HashSet<BlockPosition>());

    public Simulator(String name, IWorldProvider parent, long updateInterval) {
        _updateInterval = updateInterval;
        _parent = parent;
        _name = name;
    }

    public Simulator(String name, IWorldProvider parent) {
        this(name, parent, 1000);
    }

    public void addActiveBlock(BlockPosition bp) {
        _activeBlocks.add(bp);
    }


    public void simulateAll() {
        if (_running)
            return;

        _running = true;

        // Create a new thread and start processing
        Runnable r = new Runnable() {
            public void run() {
                int counter = 0;

                while (executeSimulation() && counter < 1024) {
                    counter++;
                }

                _running = false;
            }
        };

        Terasology.getInstance().submitTask(_name + "Complete", r);
    }

    public boolean simulate(boolean force) {
        if (_running)
            return false;

        long currentTime = Terasology.getInstance().getTimeInMs();

        if ((currentTime > _lastUpdate + _updateInterval || force)) {

            _running = true;

            // Create a new thread and start processing
            Runnable r = new Runnable() {
                public void run() {
                    executeSimulation();
                    _running = false;
                }
            };

            Terasology.getInstance().submitTask(_name, r);

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