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
package com.github.begla.blockmania.tools;

import com.github.begla.blockmania.blueprints.BlockGrid;
import com.github.begla.blockmania.blueprints.Blueprint;
import com.github.begla.blockmania.blueprints.BlueprintGenerator;
import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.intersections.RayBlockIntersection;
import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.world.characters.Player;
import javolution.util.FastList;

import java.util.logging.Level;

/**
 * TODO
 */
public class RectangleSelectionTool implements Tool {

    private Player _player;

    private FastList<BlockPosition> _selectedBlocks = new FastList<BlockPosition>();
    private BlockGrid _blockGrid = new BlockGrid();

    private Blueprint _currentBlueprint = null;

    public RectangleSelectionTool(Player player) {
        _player = player;

        Blockmania.getInstance().addRenderableObject(_blockGrid);
    }

    public void executeLeftClickAction() {
        RayBlockIntersection.Intersection is = _player.calcSelectedBlock();

        if (is == null)
            return;

        if (_currentBlueprint == null)
            addBlock(is.getBlockPosition());
        else
           _currentBlueprint.build(_player.getParent().getWorldProvider(), is.getBlockPosition());
    }

    public void executeRightClickAction() {
        reset();
    }

    private void addBlock(BlockPosition blockPosition) {
        if (_selectedBlocks.size() >= 2) {
            _selectedBlocks.clear();
            _blockGrid.clear();
        }

        _blockGrid.addGridPosition(blockPosition);
        _selectedBlocks.add(blockPosition);

        if (_selectedBlocks.size() == 2)
            generateRectangle();

        Blockmania.getInstance().getLogger().log(Level.INFO, "Added vertex block vertex at: " + blockPosition);
    }

    private void generateRectangle() {
        if (_selectedBlocks.size() < 2)
            return;

        int maxX = Math.max(_selectedBlocks.get(0).x, _selectedBlocks.get(1).x);
        int minX = Math.min(_selectedBlocks.get(0).x, _selectedBlocks.get(1).x);
        int maxY = Math.max(_selectedBlocks.get(0).y, _selectedBlocks.get(1).y);
        int minY = Math.min(_selectedBlocks.get(0).y, _selectedBlocks.get(1).y);
        int maxZ = Math.max(_selectedBlocks.get(0).z, _selectedBlocks.get(1).z);
        int minZ = Math.min(_selectedBlocks.get(0).z, _selectedBlocks.get(1).z);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (_player.getParent().getWorldProvider().getBlock(x, y, z) != 0x0) {
                        BlockPosition bp = new BlockPosition(x, y, z);

                        _selectedBlocks.add(bp);
                        _blockGrid.addGridPosition(bp);
                    }
                }
            }
        }

        _currentBlueprint = BlueprintGenerator.getInstance().generateBlueprint(_player.getParent().getWorldProvider(), _selectedBlocks);
    }

    private void reset() {
        _currentBlueprint = null;
        _selectedBlocks.clear();
        _blockGrid.clear();
    }
}
