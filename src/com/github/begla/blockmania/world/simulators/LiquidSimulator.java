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

import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.world.chunk.Chunk;
import com.github.begla.blockmania.world.interfaces.WorldProvider;

import javax.vecmath.Vector3f;
import java.util.ArrayList;

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
        super(parent, 100);
    }

    @Override
    public boolean executeSimulation() {
        ArrayList<BlockPosition> currentActiveBlocks = new ArrayList<BlockPosition>(_activeBlocks);

        boolean simulated = false;
        int counter = 0;
        for (int i = currentActiveBlocks.size() - 1; i >= 0 && counter < 4; i--, counter++) {
            simulated = true;

            BlockPosition bp = currentActiveBlocks.get(i);
            BlockPosition bpd = new BlockPosition(bp.x, bp.y - 1, bp.z);

            _activeBlocks.remove(bp);

            byte state = _parent.getState(bp.x, bp.y, bp.z);
            byte type = _parent.getBlock(bp.x, bp.y, bp.z);
            byte typeBelow = _parent.getBlock(bpd.x, bpd.y, bpd.z);

            if (state >= 1) {
                int minState = Integer.MAX_VALUE;

                for (int j = 0; j < 6; j++) {
                    byte nType = _parent.getBlock((int) NEIGHBORS6[j].x * j + bp.x, (int) NEIGHBORS6[j].y * j + bp.y, (int) NEIGHBORS6[j].z * j + bp.z);
                    byte nState = _parent.getState((int) NEIGHBORS6[j].x * j + bp.x, (int) NEIGHBORS6[j].y * j + bp.y, (int) NEIGHBORS6[j].z * j + bp.z);

                    if (nType == type) {
                        if (nState < minState)
                            minState = nState;
                    }
                }

                if (minState + 1 < state) {
                    state--;
                } else if (minState + 1 > state) {
                    state++;
                }
            }

            _parent.setState(bp.x, bp.y, bp.z, state);

            if ((typeBelow == 0 || BlockManager.getInstance().getBlock(typeBelow).getBlockForm() == Block.BLOCK_FORM.BILLBOARD)) {
                _parent.setBlock(bpd.x, bpd.y, bpd.z, type, true, true);
                _parent.setState(bpd.x, bpd.y, bpd.z, (byte) 1);
                addActiveBlock(bpd);
                continue;
            }

            if (state > 7) {
                _parent.setBlock(bp.x, bp.y, bp.z, (byte) 0, true, true);
                _parent.setState(bp.x, bp.y, bp.z, (byte) 0);
                continue;
            }

            for (int k = 0; k < 4; k++) {
                BlockPosition nBp = new BlockPosition((int) NEIGHBORS4[k].x + bp.x, bp.y, (int) NEIGHBORS4[k].z + bp.z);
                byte nBpType = _parent.getBlock(nBp.x, nBp.y, nBp.z);

                if ((nBpType == 0 || BlockManager.getInstance().getBlock(nBpType).getBlockForm() == Block.BLOCK_FORM.BILLBOARD) && _parent.getBlock(nBp.x, nBp.y - 1, nBp.z) != type) {
                    _parent.setBlock(nBp.x, nBp.y, nBp.z, type, true, true);
                    _parent.setState(nBp.x, nBp.y, nBp.z, (byte) (state + 1));
                    addActiveBlock(nBp);
                }
            }
        }

        return simulated;
    }

    public void lightChanged(Chunk chunk, BlockPosition pos) {
    }

    public void blockPlaced(Chunk chunk, BlockPosition pos) {
        if (BlockManager.getInstance().getBlock(_parent.getBlock(pos.x, pos.y, pos.z)).isLiquid()) {
            chunk.setState(pos.x, pos.y, pos.z, (byte) 1);
            addActiveBlock(pos);
        }
    }

    public void blockRemoved(Chunk chunk, BlockPosition pos) {
        for (int i = 0; i < 6; i++) {
            BlockPosition nBp = new BlockPosition(pos.x + (int) NEIGHBORS6[i].x, pos.y + (int) NEIGHBORS6[i].y, pos.z + (int) NEIGHBORS6[i].z);

            if (BlockManager.getInstance().getBlock(_parent.getBlock(nBp.x, nBp.y, nBp.z)).isLiquid()) {
                addActiveBlock(nBp);
            }
        }
    }
}
