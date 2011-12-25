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
import com.github.begla.blockmania.model.blocks.Block;
import com.github.begla.blockmania.model.blocks.BlockManager;
import com.github.begla.blockmania.model.inventory.BlockItem;
import com.github.begla.blockmania.model.structures.BlockPosition;
import com.github.begla.blockmania.model.structures.RayBlockIntersection;
import com.github.begla.blockmania.rendering.world.WorldRenderer;
import com.github.begla.blockmania.utilities.MathHelper;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * The basic tool used for block interaction. Can be used to place and remove blocks.
 */
public class DefaultBlockTool implements Tool {

    protected final Player _player;

    public DefaultBlockTool(Player player) {
        _player = player;
    }

    public void executeLeftClickAction() {
        if (_player.getActiveBlock() != null) {
            if (placeBlock(_player.getActiveBlock().getId())) {
                _player.getInventory().removeOneItemInSlot(_player.getToolbar().getSelectedSlot());
            }
        }
    }

    public void executeRightClickAction() {
        byte removedBlockId = removeBlock(false);

        if (removedBlockId != 0) {
            _player.getInventory().storeItemInFreeSlot(new BlockItem(_player, removedBlockId, 1));
        }
    }

    /**
     * Places a block of a given type in front of the player.
     *
     * @param type The type of the block
     * @return True if a block was placed
     */
    public boolean placeBlock(byte type) {
        WorldProvider worldProvider = _player.getParent().getWorldProvider();
        RayBlockIntersection.Intersection selectedBlock = _player.getSelectedBlock();

        if (selectedBlock != null) {
            Block centerBlock = BlockManager.getInstance().getBlock(worldProvider.getBlock(_player.getSelectedBlock().getBlockPosition().x, _player.getSelectedBlock().getBlockPosition().y, _player.getSelectedBlock().getBlockPosition().z));

            if (!centerBlock.isAllowBlockAttachment()) {
                return false;
            }

            BlockPosition blockPos = selectedBlock.calcAdjacentBlockPos();

            // Prevent players from placing blocks inside their bounding boxes
            if (Block.AABBForBlockAt(blockPos.x, blockPos.y, blockPos.z).overlaps(_player.getAABB())) {
                return false;
            }

            worldProvider.setBlock(blockPos.x, blockPos.y, blockPos.z, type, true, true);
            AudioManager.getInstance().playVaryingSound("PlaceBlock", 0.6f, 0.5f);

            int chunkPosX = MathHelper.calcChunkPosX(blockPos.x);
            int chunkPosZ = MathHelper.calcChunkPosZ(blockPos.z);
            _player.notifyObserversBlockPlaced(worldProvider.getChunkProvider().loadOrCreateChunk(chunkPosX, chunkPosZ), blockPos);

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
    public byte removeBlock(boolean createPhysBlock) {
        WorldProvider worldProvider = _player.getParent().getWorldProvider();
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
                        _player.setExtractionCounter((byte) (_player.getExtractionCounter() + 1));
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
                    worldProvider.setBlock(blockPos.x, blockPos.y, blockPos.z, (byte) 0x0, true, true);

                    // Remove the upper block if it's a billboard
                    byte upperBlockType = worldProvider.getBlock(blockPos.x, blockPos.y + 1, blockPos.z);
                    if (BlockManager.getInstance().getBlock(upperBlockType).getBlockForm() == Block.BLOCK_FORM.BILLBOARD) {
                        worldProvider.setBlock(blockPos.x, blockPos.y + 1, blockPos.z, (byte) 0x0, true, true);
                    }

                    worldRenderer.getBlockParticleEmitter().setOrigin(blockPos.toVector3d());
                    worldRenderer.getBlockParticleEmitter().emitParticles(256, currentBlockType);
                    AudioManager.getInstance().playVaryingSound("RemoveBlock", 0.6f, 0.5f);

                    if (createPhysBlock && !BlockManager.getInstance().getBlock(currentBlockType).isTranslucent()) {
                        Vector3d pos = blockPos.toVector3d();
                        worldRenderer.getBulletPhysicsRenderer().addBlock(new Vector3f(pos), currentBlockType);
                    }

                    int chunkPosX = MathHelper.calcChunkPosX(blockPos.x);
                    int chunkPosZ = MathHelper.calcChunkPosZ(blockPos.z);
                    _player.notifyObserversBlockRemoved(worldProvider.getChunkProvider().loadOrCreateChunk(chunkPosX, chunkPosZ), blockPos);

                    _player.resetExtraction();
                    return currentBlockType;
                }
            }

            // Play digging sound if the block was not removed
            AudioManager.getInstance().playVaryingSound("Dig", 0.6f, 0.5f);
            // ... and spread sparkles!
            worldRenderer.getBlockParticleEmitter().setOrigin(blockPos.toVector3d());
            worldRenderer.getBlockParticleEmitter().emitParticles(64, currentBlockType);
        }

        // Nothing got removed
        return 0;
    }
}
