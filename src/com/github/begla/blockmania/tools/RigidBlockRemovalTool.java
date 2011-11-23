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

import com.github.begla.blockmania.world.characters.Player;

/**
 * The basic tool used for block interaction. Can be used to place and remove blocks.
 */
public class RigidBlockRemovalTool implements Tool {

    private Player _player;

    public RigidBlockRemovalTool(Player player) {
        _player = player;
    }

    public void executeLeftClickAction() {
        _player.removeBlock(true);
    }

    public void executeRightClickAction() {
        _player.removeBlock(true);
    }
}
