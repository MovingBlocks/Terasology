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
package com.github.begla.blockmania.logic.tools;

import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.logic.characters.Player;
import com.github.begla.blockmania.model.structures.BlockPosition;
import com.github.begla.blockmania.model.structures.RayBlockIntersection;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Simple selection tool. Can be used to select multiple blocks at once.
 */
public class MultipleSelectionTool implements Tool {

    private final Player _player;
    private final ArrayList<BlockPosition> _vertexBlocks = new ArrayList<BlockPosition>();

    public MultipleSelectionTool(Player player) {
        _player = player;
    }

    public void executeLeftClickAction() {
        RayBlockIntersection.Intersection is = _player.calcSelectedBlock();

        if (is == null)
            return;

        addBlock(is.getBlockPosition());
    }

    public void executeRightClickAction() {
        RayBlockIntersection.Intersection is = _player.calcSelectedBlock();

        if (is == null)
            return;

        removeBlock(is.getBlockPosition());
    }

    private void addBlock(BlockPosition blockPosition) {
        _vertexBlocks.add(blockPosition);
        Blockmania.getInstance().getActiveWorldRenderer().getBlockGrid().addGridPosition(blockPosition);

        Blockmania.getInstance().getLogger().log(Level.INFO, "Added block at: " + blockPosition);
    }

    private void removeBlock(BlockPosition blockPosition) {
        _vertexBlocks.remove(blockPosition);
        Blockmania.getInstance().getActiveWorldRenderer().getBlockGrid().removeGridPosition(blockPosition);

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
