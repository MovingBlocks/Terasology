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
import com.github.begla.blockmania.world.interfaces.WorldProvider;

import javax.vecmath.Vector3f;

/**
 * Rough draft of Minecraft-like behavior of liquids. Will be replaced with some
 * more fancy stuff later on.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class LiquidSimulator extends Simulator {

    private static final Vector3f[] NEIGHBORS4 = {new Vector3f(-1, 0, 0), new Vector3f(1, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0, 0, -1)};
    private static final Vector3f[] NEIGHBORS6 = {new Vector3f(0, -1, 0), new Vector3f(0, 1, 0), new Vector3f(-1, 0, 0), new Vector3f(1, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0, 0, -1)};

    public LiquidSimulator(WorldProvider parent) {
        super(parent, 1000);
    }

    @Override
    public void executeSimulation() {
        for (int i = _activeBlocks.size() - 1; i >= 0; i--) {

            BlockPosition bp = _activeBlocks.get(i);
            byte state = _parent.getState(bp.x, bp.y, bp.z);
            byte type = _parent.getBlock(bp.x, bp.y, bp.z);
            byte stateAbove = _parent.getState(bp.x, bp.y + 1, bp.z);
            byte typeAbove = _parent.getBlock(bp.x, bp.y + 1, bp.z);
            byte stateBelow = _parent.getState(bp.x, bp.y - 1, bp.z);
            byte typeBelow = _parent.getBlock(bp.x, bp.y - 1, bp.z);

            // Inward flow
            if (type == typeAbove) {
                state = 1;
            } else if (state > 1) {
                int minState = Integer.MAX_VALUE;

                for (int j = 0; j < 6; j++) {
                    byte nType = _parent.getBlock((int) NEIGHBORS6[j].x * j + bp.x, (int) NEIGHBORS6[j].y * j + bp.y, (int) NEIGHBORS6[j].z * j + bp.z);
                    byte nState = _parent.getState((int) NEIGHBORS6[j].x * j + bp.x, (int) NEIGHBORS6[j].y * j + bp.y, (int) NEIGHBORS6[j].z * j + bp.z);

                    if (nType == type) {
                        if (nState < minState)
                            minState = nState;
                    }
                }

                if (minState + 1 > state) {
                    state--;
                } else if (minState + 1 < state)
                    state++;
            }

            _parent.setState(bp.x, bp.y, bp.z, state);

            if (state > 7) {
                type = 0x0;
                _parent.setBlock(bp.x, bp.y, bp.z, type, true, true, true);
                return;
            }

            if (typeBelow == 0x0) {
                _parent.setBlock(bp.x, bp.y - 1, bp.z, type, true, true, true);
                _parent.setState(bp.x, bp.y - 1, bp.z, (byte) (state + 1));
                break;
            }

            boolean[] invalid = new boolean[4];

            // Outward flow
            for (int j = 1; j < 5; j++) {

                boolean set = false;
                for (int k = 0; k < 4; k++) {
                    if (_parent.getBlock((int) NEIGHBORS4[k].x * j + bp.x, bp.y, (int) NEIGHBORS4[k].z * j + bp.z) != 0) {
                        invalid[k] = true;
                    } else if (!invalid[k]) {
                        _parent.setBlock((int) NEIGHBORS4[k].x * j + bp.x, bp.y, (int) NEIGHBORS4[k].z * j + bp.z, type, true, true, true);
                        _parent.setState((int) NEIGHBORS4[k].x * j + bp.x, bp.y, (int) NEIGHBORS4[k].z * j + bp.z, (byte) (state + 1));
                        set = true;
                    }
                }

                if (set)
                    return;
            }
        }
    }
}
