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

import com.github.begla.blockmania.logic.characters.Player;
import com.github.begla.blockmania.model.inventory.BlockItem;

/**
 * The basic tool used for block interaction. Can be used to place and remove blocks.
 */
public class RigidBlockTool extends DefaultBlockTool {

    public RigidBlockTool(Player player) {
        super(player);
    }


    public void executeRightClickAction() {
        byte removedBlockId = removeBlock(true);

        if (removedBlockId != 0) {
            _player.getInventory().storeItemInFreeSlot(new BlockItem(removedBlockId, 1));
        }
    }
}
