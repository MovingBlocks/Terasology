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
import com.github.begla.blockmania.logic.manager.AudioManager;
import com.github.begla.blockmania.logic.world.WorldProvider;
import com.github.begla.blockmania.model.blocks.BlockManager;
import com.github.begla.blockmania.model.structures.BlockPosition;
import com.github.begla.blockmania.rendering.physics.BulletPhysicsRenderer;
import com.github.begla.blockmania.rendering.world.WorldRenderer;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Creates an explosion originating from the currently selected block of the player.
 */
public class ExplosionTool implements Tool {

    private final Player _player;

    public ExplosionTool(Player player) {
        _player = player;
    }

    public void executeLeftClickAction() {
        explode();
    }

    public void executeRightClickAction() {
        // Nothing to do!
    }

    public void explode() {
        WorldProvider worldProvider = _player.getParent().getWorldProvider();

        if (_player.getSelectedBlock() != null) {
            BlockPosition blockPos = _player.getSelectedBlock().getBlockPosition();
            Vector3d origin = blockPos.toVector3d();

            int counter = 0;
            for (int i = 0; i < 512; i++) {
                Vector3d direction = new Vector3d((float) worldProvider.getRandom().randomDouble(), (float) worldProvider.getRandom().randomDouble(), (float) worldProvider.getRandom().randomDouble());
                direction.normalize();

                for (int j = 0; j < 5; j++) {
                    Vector3d target = new Vector3d(origin);

                    target.x += direction.x * j;
                    target.y += direction.y * j;
                    target.z += direction.z * j;

                    byte currentBlockType = worldProvider.getBlock((int) target.x, (int) target.y, (int) target.z);

                    if (currentBlockType != 0x0) {
                        worldProvider.setBlock((int) target.x, (int) target.y, (int) target.z, (byte) 0x0, true, true);

                        if (!BlockManager.getInstance().getBlock(currentBlockType).isTranslucent() && counter % 4 == 0)
                            BulletPhysicsRenderer.getInstance().addBlock(new Vector3f(target), currentBlockType);

                        counter++;
                    }
                }
            }

            AudioManager.getInstance().playVaryingSound("RemoveBlock", 0.3f, 1.0f);
            _player.poke();
        }
    }

}
