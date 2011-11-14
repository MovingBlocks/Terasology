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
import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.intersections.RayBlockIntersection;
import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.world.characters.Player;
import javolution.util.FastList;

import java.util.logging.Level;

/**
 * Simple selection tool. Can be used to select multiple blocks at once.
 */
public class MultipleSelectionTool implements Tool {

    private Player _player;
    private FastList<BlockPosition> _vertexBlocks = new FastList<BlockPosition>();
    private BlockGrid _blockGrid = new BlockGrid();

    public MultipleSelectionTool(Player player) {
        _player = player;

        Blockmania.getInstance().addRenderableObject(_blockGrid);
    }

    public void executeLeftClickAction() {
        RayBlockIntersection.Intersection is = _player.calcSelectedBlock();

        if (is == null)
            return;

        AddBlock(is.getBlockPosition());
    }

    public void executeRightClickAction() {
        RayBlockIntersection.Intersection is = _player.calcSelectedBlock();

        if (is == null)
            return;

        RemoveBlock(is.getBlockPosition());
    }

    private void AddBlock(BlockPosition blockPosition) {
        _vertexBlocks.add(blockPosition);
        _blockGrid.addGridPosition(blockPosition);

        Blockmania.getInstance().getLogger().log(Level.INFO, "Added block at: " + blockPosition);
    }

    private void RemoveBlock(BlockPosition blockPosition) {
        _vertexBlocks.remove(blockPosition);
        _blockGrid.removeGridPosition(blockPosition);

        Blockmania.getInstance().getLogger().log(Level.INFO, "Removed block at: " + blockPosition);
    }

    /**
     * Simple example to save the selected blocks somewhere... But where?
     */
    private void generateBlueprint() {
        for (BlockPosition bp : _vertexBlocks) {
            // Save me please :-)
            _player.getParent().getWorldProvider().getBlock(bp.x, bp.y, bp.z);
        }
    }
}
