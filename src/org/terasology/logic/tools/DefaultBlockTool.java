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
import org.terasology.math.Side;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockGroup;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.inventory.ItemBlock;
import org.terasology.model.structures.AABB;
import org.terasology.model.structures.BlockPosition;
import org.terasology.model.structures.RayBlockIntersection;
import org.terasology.rendering.physics.BulletPhysicsRenderer;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * The basic tool used for block interaction. Can be used to place and remove blocks.
 */
public class DefaultBlockTool extends SimpleTool {

    public DefaultBlockTool(Player player) {
        super(player);
    }

    public void executeLeftClickAction() {
        if (_player.getActiveBlock() != null) {
            if (placeBlock(_player.getActiveBlock())) {
                _player.getInventory().removeItemAt(_player.getInventory().getSelectedCubbyhole(), 1);
            }
        }
    }

    public void executeRightClickAction() {
        Block removedBlock = removeBlock(true);

        if (removedBlock != null) {
            // TODO: This should not be hardcoded!
            if (removedBlock.getBlockGroup().getTitle().equals("Torch"))
                _player.getInventory().addItem(new ItemBlock(removedBlock.getBlockGroup()), 1);
        }
    }

    /**
     * Places a block of a given type in front of the player.
     *
     * @param type The type of the block
     * @return True if a block was placed
     */
    public boolean placeBlock(BlockGroup type) {
        IWorldProvider worldProvider = _player.getParent().getWorldProvider();
        RayBlockIntersection.Intersection selectedBlock = _player.getSelectedBlock();

        if (selectedBlock != null) {
            BlockPosition centerPos = selectedBlock.getBlockPosition();
            Block centerBlock = BlockManager.getInstance().getBlock(worldProvider.getBlock(centerPos.x, centerPos.y, centerPos.z));

            if (!centerBlock.isAllowBlockAttachment()) {
                return false;
            }

            BlockPosition blockPos = selectedBlock.calcAdjacentBlockPos();
            Block adjBlock = BlockManager.getInstance().getBlock(worldProvider.getBlock(blockPos.x, blockPos.y, blockPos.z));
            if (adjBlock != null && !adjBlock.isInvisible() && !adjBlock.isSelectionRayThrough()) {
                return false;
            }

            // Need two things:
            // 1. The Side of attachment
            Side attachmentSide = Side.inDirection(centerPos.x - blockPos.x, centerPos.y - blockPos.y, centerPos.z - blockPos.z);
            // 2. The secondary direction
            Vector3d attachDir = new Vector3d(centerPos.x - blockPos.x, centerPos.y - blockPos.y, centerPos.z - blockPos.z);
            Vector3d rawDirection = new Vector3d(_player.getViewingDirection());
            double dot = rawDirection.dot(attachDir);
            rawDirection.sub(new Vector3d(dot * attachDir.x, dot * attachDir.y, dot * attachDir.z));
            Side direction = Side.inDirection(rawDirection.x, rawDirection.y, rawDirection.z);

            Block block = type.getBlockFor(attachmentSide, direction);
            if (block == null)
                return false;

            // Prevent players from placing blocks inside their bounding boxes
            if (!block.isPenetrable()) {
                for (AABB blockAABB : block.getColliders(blockPos.x, blockPos.y, blockPos.z)) {
                    if (blockAABB.overlaps(_player.getAABB())) {
                        return false;
                    }
                }
            }

            placeBlock(blockPos, block.getId(), true);

            AudioManager.play("PlaceBlock", 0.5f);

            return true;
        }

        return false;
    }

    /**
     * Removes a block at the given global world position.
     *
     * @param createPhysBlock Creates a rigid body block if true
     * @return The removed block (if any)
     */
    public Block removeBlock(boolean createPhysBlock) {
        IWorldProvider worldProvider = _player.getParent().getWorldProvider();
        WorldRenderer worldRenderer = _player.getParent();
        RayBlockIntersection.Intersection selectedBlock = _player.getSelectedBlock();

        if (selectedBlock != null) {

            BlockPosition blockPos = selectedBlock.getBlockPosition();
            byte currentBlockType = worldProvider.getBlock(blockPos.x, blockPos.y, blockPos.z);
            Block block = BlockManager.getInstance().getBlock(currentBlockType);

            if (block.isDestructible()) {

                // Check if this block was "poked" before
                if (_player.getExtractedBlock() != null) {
                    if (_player.getExtractedBlock().equals(_player.getSelectedBlock())) {
                        // If so increase the extraction counter
                        int amount = 1;

                        if (_player.getActiveItem() != null) {
                            amount = _player.getActiveItem().getExtraction(block);
                        }

                        _player.setExtractionCounter((byte) (_player.getExtractionCounter() + amount));
                    } else {
                        // If another block was touched in between...
                        _player.resetExtraction();
                        _player.setExtractionCounter((byte) 1);
                    }
                } else {
                    // This block was "poked" for the first time
                    _player.setExtractedBlock(_player.getSelectedBlock());
                    _player.setExtractionCounter((byte) 1);
                }

                // Enough pokes... Remove the block!
                if (_player.getExtractionCounter() >= block.getHardness()) {
                    placeBlock(blockPos, (byte) 0x0, true);

                    // Remove the upper block if it's a billboard
                    byte upperBlockType = worldProvider.getBlock(blockPos.x, blockPos.y + 1, blockPos.z);
                    if (BlockManager.getInstance().getBlock(upperBlockType).getBlockForm() == Block.BLOCK_FORM.BILLBOARD) {
                        worldProvider.setBlock(blockPos.x, blockPos.y + 1, blockPos.z, (byte) 0x0, true, true);
                    }

                    worldRenderer.getBlockParticleEmitter().setOrigin(blockPos.toVector3d());
                    worldRenderer.getBlockParticleEmitter().emitParticles(256, currentBlockType);
                    AudioManager.play("RemoveBlock", 0.6f);

                    /* PHYSICS */
                    Vector3d pos = blockPos.toVector3d();
                    _player.getParent().getBulletRenderer().addLootableBlocks(new Vector3f(pos), block);

                    _player.resetExtraction();
                    return block;
                }
            }

            // Play digging sound if the block was not removed
            AudioManager.play("Dig", 1.0f);
            // ... and spread sparkles!
            worldRenderer.getBlockParticleEmitter().setOrigin(blockPos.toVector3d());
            worldRenderer.getBlockParticleEmitter().emitParticles(64, currentBlockType);
        }

        // Nothing got removed
        return null;
    }
}
