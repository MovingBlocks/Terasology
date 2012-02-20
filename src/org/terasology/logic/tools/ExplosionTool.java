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
package org.terasology.logic.tools;

import org.terasology.logic.characters.Player;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.BlockPosition;
import org.terasology.rendering.physics.BulletPhysicsRenderer;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Creates an explosion originating from the currently selected block of the player.
 */
public class ExplosionTool extends SimpleTool {

    public ExplosionTool(Player player) {
        super(player);
    }

    public void executeLeftClickAction() {
        explode();
    }

    public void executeRightClickAction() {
        // Nothing to do!
    }

    public void explode() {
        IWorldProvider worldProvider = _player.getParent().getWorldProvider();

        if (_player.getSelectedBlock() != null) {
            BlockPosition blockPos = _player.getSelectedBlock().getBlockPosition();
            Vector3d origin = blockPos.toVector3d();

            for (int i = 0; i < 256; i++) {
                Vector3d direction = new Vector3d((float) worldProvider.getRandom().randomDouble(), (float) worldProvider.getRandom().randomDouble(), (float) worldProvider.getRandom().randomDouble());
                direction.normalize();
                Vector3f impulse = new Vector3f(direction);
                impulse.scale(800000);

                for (int j = 0; j < 4; j++) {
                    Vector3f target = new Vector3f(origin);

                    target.x += direction.x * j;
                    target.y += direction.y * j;
                    target.z += direction.z * j;

                    byte currentBlockType = worldProvider.getBlock((int) target.x, (int) target.y, (int) target.z);

                    if (currentBlockType == 0x0)
                        continue;

                    Block currentBlock = BlockManager.getInstance().getBlock(currentBlockType);

                    /* PHYSICS */
                    if (currentBlock.isDestructible()) {
                        // Make sure no updates are triggered
                        placeBlock((int) target.x, (int) target.y, (int) target.z, (byte) 0x0, false);
                        BulletPhysicsRenderer.getInstance().addTemporaryBlock(target, currentBlockType, impulse, BulletPhysicsRenderer.BLOCK_SIZE.FULL_SIZE);
                    }
                }
            }

            AudioManager.getInstance().getAudio("Explode" + (worldProvider.getRandom().randomIntAbs(5) + 1)).playAsSoundEffect(1.0f, 0.2f, false);
            _player.poke();
        }
    }

}
